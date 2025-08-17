package snownee.kiwi.customization.builder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import snownee.kiwi.Kiwi;
import snownee.kiwi.customization.block.KBlockUtils;
import snownee.kiwi.customization.block.family.BlockFamily;

public record CyclePropertyRule(
		Map<BlockFamily, Map<String, String>> families,
		BlockSpread spread,
		Map<Block, Map<Property<?>, Set<Object>>> blocks) implements BuilderRule {
	public static final MapCodec<CyclePropertyRule> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
					Codec.unboundedMap(BlockFamily.CODEC, Codec.unboundedMap(Codec.STRING, Codec.STRING))
							.fieldOf("family")
							.forGetter(CyclePropertyRule::families), BlockSpread.CODEC.fieldOf("spread").forGetter(CyclePropertyRule::spread))
			.apply(instance, CyclePropertyRule::of));

	public static CyclePropertyRule of(Map<BlockFamily, Map<String, String>> families, BlockSpread spread) {
		ImmutableMap.Builder<Block, Map<Property<?>, Set<Object>>> blocks = ImmutableMap.builder();
		Interner<Map<Property<?>, Set<Object>>> interner = Interners.newStrongInterner();
		Interner<Set<Object>> valuesInterner = Interners.newStrongInterner();
		for (Map.Entry<BlockFamily, Map<String, String>> entry : families.entrySet()) {
			for (Block block : entry.getKey().blocks().toList()) {
				ImmutableMap.Builder<Property<?>, Set<Object>> properties = ImmutableMap.builder();
				for (Map.Entry<String, String> propEntry : entry.getValue().entrySet()) {
					try {
						Property<?> property = KBlockUtils.getProperty(block.defaultBlockState(), propEntry.getKey());
						if (propEntry.getValue().equals("*")) {
							properties.put(property, Set.of());
							continue;
						}
						String[] values = StringUtils.split(propEntry.getValue(), '|');
						ImmutableSet.Builder<Object> setBuilder = ImmutableSet.builder();
						for (String value : values) {
							Optional<?> opt = property.getValue(value);
							if (opt.isPresent()) {
								setBuilder.add(opt.get());
							} else {
								Kiwi.LOGGER.warn("Invalid value {} for property {} on block {}", value, property, block);
							}
						}
						ImmutableSet<Object> set = setBuilder.build();
						if (!set.isEmpty()) {
							properties.put(property, valuesInterner.intern(set));
						}
					} catch (Exception ignored) {
					}
				}
				ImmutableMap<Property<?>, Set<Object>> map = properties.build();
				if (!map.isEmpty()) {
					blocks.put(block, interner.intern(map));
				}
			}
		}
		return new CyclePropertyRule(families, spread, blocks.build());
	}

	@Override
	public Type<?> type() {
		return BuilderRuleTypes.CYCLE_PROPERTY.getOrCreate();
	}

	@Override
	public Stream<Block> relatedBlocks() {
		return blocks.keySet().stream();
	}

	@Override
	public boolean matches(Player player, ItemStack itemStack, BlockState blockState) {
		return true;
	}

	@Override
	public void apply(UseOnContext context, List<BlockPos> positions) {
		Player player = context.getPlayer();
		Level level = context.getLevel();
		boolean success = false;
		Map<Block, BlockState> usedBlocks = Maps.newHashMap();
		for (BlockPos pos : positions) {
			BlockState oldBlock = level.getBlockState(pos);
			BlockState newBlock = oldBlock;
			Block block = oldBlock.getBlock();
			BlockState usedBlock = usedBlocks.get(block);
			props:
			for (Map.Entry<Property<?>, Set<Object>> propEntry : blocks.get(block).entrySet()) {
				Property<?> property = propEntry.getKey();
				Set<Object> values = propEntry.getValue();
				Object curValue = oldBlock.getValue(property);
				if (!values.isEmpty() && !values.contains(curValue)) {
					continue;
				}
				Object value = usedBlock == null ? null : usedBlock.getValue(property);
				if (!values.isEmpty() && !values.contains(value)) {
					value = null;
				}
				if (value == null) {
					do {
						//noinspection rawtypes,unchecked
						newBlock = newBlock.cycle((Property) property);
						value = newBlock.getValue(property);
						if (value.equals(curValue)) {
							continue props;
						}
					} while (!values.isEmpty() && !values.contains(value));
				} else {
					//noinspection rawtypes,unchecked
					newBlock = newBlock.setValue((Property) property, (Comparable) value);
				}
			}
			if (!newBlock.canSurvive(level, pos)) {
				continue;
			}
			if (usedBlock == null) {
				usedBlocks.put(block, newBlock);
			}
			success |= level.setBlock(pos, newBlock, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
		}
		if (success && player != null) {
			for (BlockState block : usedBlocks.values()) {
				playPlaceSound(player, block);
			}
		}
	}

	@Override
	public List<BlockPos> searchPositions(BlockState blockState, UseOnContext context) {
		List<BlockPos> list = List.of();
		Map<Block, BlockState> usedBlocks = Maps.newHashMap();
		try {
			list = spread.collect(
					context, $ -> {
						Block block = $.getBlock();
						Map<Property<?>, Set<Object>> map = blocks.get(block);
						if (map == null) {
							return false;
						}
						BlockState usedBlock = usedBlocks.get(block);
						for (Map.Entry<Property<?>, Set<Object>> entry : map.entrySet()) {
							Property<?> property = entry.getKey();
							Set<Object> values = entry.getValue();
							Object curValue = $.getValue(property);
							if (usedBlock != null) {
								if (!values.isEmpty() && !values.contains(curValue)) {
									continue;
								}
								if (!curValue.equals(usedBlock.getValue(property))) {
									return false;
								}
							}
						}
						if (usedBlock != $) {
							if (usedBlock != null) {
								for (Map.Entry<Property<?>, Set<Object>> entry : map.entrySet()) {
									Property<?> property = entry.getKey();
									Set<Object> values = entry.getValue();
									Object value = $.getValue(property);
									if (!values.isEmpty() && !values.contains(value)) {
										continue;
									}
									//noinspection rawtypes,unchecked
									usedBlock = usedBlock.setValue((Property) property, (Comparable) value);
								}
							}
							usedBlocks.put(block, usedBlock);
						}
						return true;
					}, null);
		} catch (Exception e) {
			Kiwi.LOGGER.error("Failed to collect positions", e);
		}
		return list;
	}
}