package snownee.kiwi.customization.builder;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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

public class CyclePropertyRule implements BuilderRule {
	public static final MapCodec<CyclePropertyRule> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
					Codec.unboundedMap(BlockFamily.DIRECT_CODEC, Codec.STRING).fieldOf("family").forGetter(CyclePropertyRule::families),
					BlockSpread.CODEC.fieldOf("spread").forGetter(CyclePropertyRule::spread))
			.apply(instance, CyclePropertyRule::new));

	final Map<BlockFamily, String> families;
	final BlockSpread spread;
	final Map<Block, Property<?>> blocks;

	public CyclePropertyRule(Map<BlockFamily, String> families, BlockSpread spread) {
		this.families = families;
		this.spread = spread;
		blocks = Maps.newLinkedHashMap();
		for (Map.Entry<BlockFamily, String> entry : families.entrySet()) {
			String propertyName = entry.getValue();
			for (Block block : entry.getKey().blocks().toList()) {
				try {
					Property<?> property = KBlockUtils.getProperty(block.defaultBlockState(), propertyName);
					blocks.put(block, property);
				} catch (Exception ignored) {
				}
			}
		}
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
		Map<Block, Object> usedBlocks = Maps.newHashMap();
		for (BlockPos pos : positions) {
			BlockState oldBlock = level.getBlockState(pos);
			Block block = oldBlock.getBlock();
			Property<?> property = blocks.get(block);
			if (property == null) {
				continue;
			}
			Object value = usedBlocks.get(block);
			if (value == null) {
				value = oldBlock.cycle(property).getValue(property);
				usedBlocks.put(block, value);
			} else if (value == oldBlock.getValue(property)) {
				continue;
			}
			//noinspection rawtypes,unchecked
			BlockState newBlock = oldBlock.setValue((Property) property, (Comparable) value);
			if (!newBlock.canSurvive(level, pos)) {
				continue;
			}
			level.setBlock(pos, newBlock, Block.UPDATE_CLIENTS);
			success = true;
		}
		if (success && player != null) {
			for (Block block : usedBlocks.keySet()) {
				playPlaceSound(player, block.defaultBlockState());
			}
		}
	}

	@Override
	public List<BlockPos> searchPositions(BlockState blockState, UseOnContext context) {
		List<BlockPos> list = List.of();
		Map<Block, Object> usedBlocks = Maps.newHashMap();
		try {
			list = spread.collect(
					context, $ -> {
						Block block = $.getBlock();
						if (!blocks.containsKey(block)) {
							return false;
						}
						Property<?> property = blocks.get(block);
						Object value = usedBlocks.get(block);
						return value == null || value.equals($.getValue(property));
					}, (pos, $) -> usedBlocks.computeIfAbsent($.getBlock(), block -> $.getValue(blocks.get(block))));
		} catch (Exception e) {
			Kiwi.LOGGER.error("Failed to collect positions", e);
		}
		return list;
	}

	public Map<BlockFamily, String> families() {
		return families;
	}

	public BlockSpread spread() {
		return spread;
	}
}
