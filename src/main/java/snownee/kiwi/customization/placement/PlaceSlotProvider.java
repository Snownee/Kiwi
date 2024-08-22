package snownee.kiwi.customization.placement;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.apache.commons.lang3.mutable.MutableObject;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import snownee.kiwi.Kiwi;
import snownee.kiwi.customization.block.KBlockUtils;
import snownee.kiwi.customization.block.loader.KBlockDefinition;
import snownee.kiwi.customization.block.loader.KBlockTemplate;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.util.KHolder;
import snownee.kiwi.util.KUtil;
import snownee.kiwi.util.codec.CustomizationCodecs;
import snownee.kiwi.util.codec.KCodecs;

public record PlaceSlotProvider(
		List<PlaceTarget> target,
		Optional<String> transformWith,
		List<String> tag,
		List<Slot> slots) {
	public static final Predicate<String> TAG_PATTERN = Pattern.compile("^[*@]?(?:[a-z0-9_/.]+:)?[a-z0-9_/.]+$").asPredicate();
	public static final Codec<String> TAG_CODEC = Codec.STRING.validate(s -> {
		if (TAG_PATTERN.test(s)) {
			return DataResult.success(s);
		} else {
			return DataResult.error(() -> "Bad tag format: " + s);
		}
	});
	public static final Codec<PlaceSlotProvider> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			KCodecs.compactList(PlaceTarget.CODEC).fieldOf("target").forGetter(PlaceSlotProvider::target),
			Codec.STRING.optionalFieldOf("transform_with").forGetter(PlaceSlotProvider::transformWith),
			TAG_CODEC.listOf().optionalFieldOf("tag", List.of()).forGetter(PlaceSlotProvider::tag),
			Slot.CODEC.listOf().fieldOf("slots").forGetter(PlaceSlotProvider::slots)
	).apply(instance, PlaceSlotProvider::new));

	public record Slot(
			List<StatePropertiesPredicate> when,
			Optional<String> transformWith,
			List<String> tag,
			Map<Direction, Side> sides) {
		public static final Codec<Slot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ExtraCodecs.nonEmptyList(KCodecs.compactList(StatePropertiesPredicate.CODEC))
						.optionalFieldOf("when", List.of())
						.forGetter(Slot::when),
				Codec.STRING.optionalFieldOf("transform_with").forGetter(Slot::transformWith),
				TAG_CODEC.listOf().optionalFieldOf("tag", List.of()).forGetter(Slot::tag),
				Codec.unboundedMap(CustomizationCodecs.DIRECTION, Side.CODEC)
						.xmap(Map::copyOf, Function.identity())
						.fieldOf("sides")
						.forGetter(Slot::sides)
		).apply(instance, Slot::new));
	}

	public record Side(List<String> tag) {
		public static final Codec<Side> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				TAG_CODEC.listOf().optionalFieldOf("tag", List.of()).forGetter(Side::tag)
		).apply(instance, Side::new));
	}

	public record Preparation(
			Map<ResourceLocation, PlaceSlotProvider> providers,
			ListMultimap<KBlockTemplate, KHolder<PlaceSlotProvider>> byTemplate,
			ListMultimap<ResourceLocation, KHolder<PlaceSlotProvider>> byBlock,
			ListMultimap<Pair<BlockState, Direction>, PlaceSlot> slots,
			Interner<PlaceSlot> slotInterner,
			Set<Block> accessedBlocks,
			Set<String> knownPrimaryTags) {
		public static Preparation of(
				Supplier<Map<ResourceLocation, PlaceSlotProvider>> providersSupplier,
				Map<ResourceLocation, KBlockTemplate> templates) {
			Map<ResourceLocation, PlaceSlotProvider> providers = Platform.isDataGen() ? Map.of() : providersSupplier.get();
			ListMultimap<KBlockTemplate, KHolder<PlaceSlotProvider>> byTemplate = ArrayListMultimap.create();
			ListMultimap<ResourceLocation, KHolder<PlaceSlotProvider>> byBlock = ArrayListMultimap.create();
			for (var entry : providers.entrySet()) {
				KHolder<PlaceSlotProvider> holder = new KHolder<>(entry.getKey(), entry.getValue());
				for (PlaceTarget target : holder.value().target) {
					switch (target.type()) {
						case TEMPLATE -> {
							KBlockTemplate template = templates.get(target.id());
							if (template == null) {
								Kiwi.LOGGER.error("Template {} not found for slot provider {}", target.id(), holder);
								continue;
							}
							byTemplate.put(template, holder);
						}
						case BLOCK -> byBlock.put(target.id(), holder);
					}
				}
			}
			return new Preparation(
					providers,
					byTemplate,
					byBlock,
					ArrayListMultimap.create(),
					Interners.newStrongInterner(),
					Sets.newHashSet(),
					Sets.newHashSet());
		}

		public void attachSlotsA(Block block, KBlockDefinition definition) {
			// why? because the Forge's registry will give you duplicate entries sometimes
			if (!accessedBlocks.add(block)) {
				return;
			}
			for (KHolder<PlaceSlotProvider> holder : byTemplate.get(definition.template().template())) {
				try {
					holder.value().attachSlots(this, block);
				} catch (Exception e) {
					Kiwi.LOGGER.error("Failed to attach slots for block %s with provider %s".formatted(block, holder), e);
				}
			}
		}

		public void attachSlotsB() {
			byBlock.asMap().forEach((blockId, holders) -> {
				Block block = BuiltInRegistries.BLOCK.get(blockId);
				if (block == Blocks.AIR) {
					Kiwi.LOGGER.error("Block %s not found for slot providers %s".formatted(blockId, holders));
					return;
				}
				for (KHolder<PlaceSlotProvider> holder : holders) {
					try {
						holder.value().attachSlots(this, block);
					} catch (Exception e) {
						Kiwi.LOGGER.error("Failed to attach slots for block %s with provider %s".formatted(block, holder), e);
					}
				}
			});
			PlaceSlot.renewData(this);
		}

		public void register(BlockState blockState, PlaceSlot placeSlot) {
			if (blockState.hasProperty(BlockStateProperties.WATERLOGGED) && blockState.getValue(BlockStateProperties.WATERLOGGED)) {
				throw new IllegalArgumentException("Waterlogged block state is not supported: %s".formatted(blockState));
			}
			Pair<BlockState, Direction> key = Pair.of(blockState, placeSlot.side());
			Collection<PlaceSlot> slots = slots().get(key);
			if (!slots.isEmpty()) {
				String primaryTag = placeSlot.primaryTag();
				Optional<PlaceSlot> any = slots.stream().filter(slot -> slot.primaryTag().equals(primaryTag)).findAny();
				if (any.isPresent()) {
					throw new IllegalArgumentException("Primary tag %s conflict: %s and %s".formatted(primaryTag, placeSlot, any.get()));
				}
			}
			placeSlot = slotInterner.intern(placeSlot);
			slots().put(key, placeSlot);
			knownPrimaryTags().add(placeSlot.primaryTag());
		}
	}

	private void attachSlots(Preparation preparation, Block block) {
		for (Slot slot : this.slots) {
			for (BlockState blockState : block.getStateDefinition().getPossibleStates()) {
				if (blockState.hasProperty(BlockStateProperties.WATERLOGGED) && blockState.getValue(BlockStateProperties.WATERLOGGED)) {
					continue;
				}
				if (!slot.when.isEmpty() && slot.when.stream().noneMatch(predicate -> predicate.test(blockState))) {
					continue;
				}
				for (Direction direction : KUtil.DIRECTIONS) {
					Side side = slot.sides.get(direction);
					if (side == null) {
						continue;
					}
					PlaceSlot placeSlot = new PlaceSlot(direction, generateTags(slot, side, blockState, Rotation.NONE));
					preparation.register(blockState, placeSlot);
				}
				String transformWith = (slot.transformWith.isPresent() ? slot.transformWith : this.transformWith).orElse("none");
				if (!"none".equals(transformWith)) {
					Property<?> property = KBlockUtils.getProperty(blockState, transformWith);
					if (!(property instanceof DirectionProperty directionProperty)) {
						throw new IllegalArgumentException("Invalid transform_with property: " + transformWith);
					}
					attachSlotWithTransformation(preparation, slot, blockState, directionProperty);
				}
			}
		}
	}

	private void attachSlotWithTransformation(Preparation preparation, Slot slot, BlockState blockState, DirectionProperty property) {
		Direction baseDirection = blockState.getValue(property);
		BlockState rotatedState = blockState;
		while ((rotatedState = rotatedState.cycle(property)) != blockState) {
			Direction newDirection = rotatedState.getValue(property);
			if (Direction.Plane.VERTICAL.test(newDirection)) {
				continue;
			}
			Rotation rotation = null;
			for (Rotation value : Rotation.values()) {
				if (value.rotate(baseDirection) == newDirection) {
					rotation = value;
					break;
				}
			}
			if (rotation == null) {
				throw new IllegalStateException("Invalid direction: " + newDirection);
			}
			for (Direction direction : KUtil.DIRECTIONS) {
				Side side = slot.sides.get(direction);
				if (side == null) {
					continue;
				}
				PlaceSlot placeSlot = new PlaceSlot(rotation.rotate(direction), generateTags(slot, side, rotatedState, rotation));
				preparation.register(rotatedState, placeSlot);
			}
		}
	}

	private ImmutableSortedMap<String, String> generateTags(Slot slot, Side side, BlockState rotatedState, Rotation rotation) {
		Map<String, String> map = Maps.newHashMap();
		MutableObject<String> primaryKey = new MutableObject<>();
		Streams.concat(tag.stream(), slot.tag.stream(), side.tag.stream()).forEach(s -> {
			ParsedProtoTag tag = ParsedProtoTag.of(s).resolve(rotatedState, rotation);
			if (tag.prefix().equals("*")) {
				if (primaryKey.getValue() == null) {
					primaryKey.setValue(tag.key());
				} else if (!Objects.equals(primaryKey.getValue(), tag.key())) {
					throw new IllegalArgumentException("Only one primary tag is allowed");
				}
			}
			map.put(tag.key(), tag.value());
		});
		if (primaryKey.getValue() == null) {
			throw new IllegalArgumentException("Primary tag is required");
		}
		String primaryValue = map.get(primaryKey.getValue());
		map.remove(primaryKey.getValue());
		if (primaryValue.isEmpty()) {
			map.put("*%s".formatted(primaryKey.getValue()), "");
		} else {
			map.put("*%s:%s".formatted(primaryKey.getValue(), primaryValue), "");
		}
		return ImmutableSortedMap.copyOf(map, PlaceSlot.TAG_COMPARATOR);
	}
}
