package snownee.kiwi.util.codec;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import snownee.kiwi.RenderLayerEnum;
import snownee.kiwi.customization.block.loader.BlockCodecs;

public class CustomizationCodecs {
	public static final BiMap<String, NoteBlockInstrument> INSTRUMENTS = HashBiMap.create();
	public static final Codec<NoteBlockInstrument> INSTRUMENT_CODEC = simpleByNameCodec(INSTRUMENTS);
	public static final BiMap<String, MapColor> MAP_COLORS = HashBiMap.create();
	public static final Codec<MapColor> MAP_COLOR_CODEC = simpleByNameCodec(MAP_COLORS);
	public static final Codec<PushReaction> PUSH_REACTION = simpleByNameCodec(ImmutableBiMap.of(
			"normal", PushReaction.NORMAL,
			"destroy", PushReaction.DESTROY,
			"block", PushReaction.BLOCK,
			"ignore", PushReaction.IGNORE,
			"push_only", PushReaction.PUSH_ONLY));
	public static final Codec<RenderLayerEnum> RENDER_TYPE = Codec.STRING.flatXmap(
			s -> switch (s) {
				case "cutout", "cutout_mipped" -> DataResult.success(RenderLayerEnum.CUTOUT);
				case "translucent" -> DataResult.success(RenderLayerEnum.TRANSLUCENT);
				default -> DataResult.error(() -> "Unknown render type: " + s);
			}, type -> switch (type) {
				case CUTOUT -> DataResult.success("cutout");
				case TRANSLUCENT -> DataResult.success("translucent");
			});
	public static final Codec<BlockBehaviour.OffsetType> OFFSET_TYPE = simpleByNameCodec(ImmutableBiMap.of(
			"xz", BlockBehaviour.OffsetType.XZ,
			"xyz", BlockBehaviour.OffsetType.XYZ));
	public static final Codec<BlockBehaviour.StatePredicate> STATE_PREDICATE = Codec.BOOL.flatComapMap(
			bl -> {
				return bl ? Blocks::always : Blocks::never;
			}, _ -> {
				return DataResult.error(() -> "Unsupported operation");
			});
	public static final Codec<TreeGrower> TREE_GROWER = Codec.withAlternative(
			TreeGrower.CODEC,
			RecordCodecBuilder.create(instance -> instance.group(
					Codec.STRING.fieldOf("name").forGetter(BlockCodecs::notImplemented),
					Codec.FLOAT.optionalFieldOf("secondary_chance", 0F).forGetter(BlockCodecs::notImplemented),
					ResourceKey.codec(Registries.CONFIGURED_FEATURE).optionalFieldOf("mega_tree").forGetter(BlockCodecs::notImplemented),
					ResourceKey.codec(Registries.CONFIGURED_FEATURE)
							.optionalFieldOf("secondary_mega_tree")
							.forGetter(BlockCodecs::notImplemented),
					ResourceKey.codec(Registries.CONFIGURED_FEATURE).optionalFieldOf("tree").forGetter(BlockCodecs::notImplemented),
					ResourceKey.codec(Registries.CONFIGURED_FEATURE)
							.optionalFieldOf("secondary_tree")
							.forGetter(BlockCodecs::notImplemented),
					ResourceKey.codec(Registries.CONFIGURED_FEATURE).optionalFieldOf("flowers").forGetter(BlockCodecs::notImplemented),
					ResourceKey.codec(Registries.CONFIGURED_FEATURE)
							.optionalFieldOf("secondary_flowers")
							.forGetter(BlockCodecs::notImplemented)
			).apply(instance, TreeGrower::new)));
	// TODO BlockPredicate has its own Codec now.
	//  However, to use that, you need to wrap your JsonOps into RegistryOps, which requires a HolderLookup.Provider.
	//  Meaning, you need to get HolderLookup.Provider somewhere.
	public static final Codec<BlockPredicate> BLOCK_PREDICATE = new Codec<>() {
		@Override
		public <T> DataResult<Pair<BlockPredicate, T>> decode(DynamicOps<T> ops, T input) {
			String stringValue = ops.getStringValue(input).result().orElse(null);
			if (stringValue != null) {
				if (stringValue.startsWith("#")) {
					return DataResult.success(Pair.of(
							BlockPredicate.Builder.block()
									.of(
											BuiltInRegistries.BLOCK,
											TagKey.create(Registries.BLOCK, Identifier.parse(stringValue.substring(1))))
									.build(), ops.empty()));
				}
				return DataResult.success(Pair.of(
						BlockPredicate.Builder.block()
								.of(
										BuiltInRegistries.BLOCK,
										BuiltInRegistries.BLOCK.get(Identifier.parse(stringValue)).orElseThrow().value())
								.build(), ops.empty()));
			}
			//return ExtraCodecs.JSON.decode(ops, input).map($ -> $.mapFirst(BlockPredicate::fromJson));
			return DataResult.error(() -> "Raw JSON input is not supported");
		}

		@Override
		public <T> DataResult<T> encode(BlockPredicate input, DynamicOps<T> ops, T prefix) {
			//return ExtraCodecs.JSON.encodeStart(ops, input.serializeToJson());
			return DataResult.error(() -> "Not supported yet");
		}
	};

	static {
		// ^.+ ([a-zA-Z].+?) ([A-Z_]+) = new [a-zA-Z].+;
		// ->
		// MAP_COLORS.put("\L$2\E", $1.$2);
		MAP_COLORS.put("none", MapColor.NONE);
		MAP_COLORS.put("grass", MapColor.GRASS);
		MAP_COLORS.put("sand", MapColor.SAND);
		MAP_COLORS.put("wool", MapColor.WOOL);
		MAP_COLORS.put("fire", MapColor.FIRE);
		MAP_COLORS.put("ice", MapColor.ICE);
		MAP_COLORS.put("metal", MapColor.METAL);
		MAP_COLORS.put("plant", MapColor.PLANT);
		MAP_COLORS.put("snow", MapColor.SNOW);
		MAP_COLORS.put("clay", MapColor.CLAY);
		MAP_COLORS.put("dirt", MapColor.DIRT);
		MAP_COLORS.put("stone", MapColor.STONE);
		MAP_COLORS.put("water", MapColor.WATER);
		MAP_COLORS.put("wood", MapColor.WOOD);
		MAP_COLORS.put("quartz", MapColor.QUARTZ);
		MAP_COLORS.put("color_orange", MapColor.COLOR_ORANGE);
		MAP_COLORS.put("color_magenta", MapColor.COLOR_MAGENTA);
		MAP_COLORS.put("color_light_blue", MapColor.COLOR_LIGHT_BLUE);
		MAP_COLORS.put("color_yellow", MapColor.COLOR_YELLOW);
		MAP_COLORS.put("color_light_green", MapColor.COLOR_LIGHT_GREEN);
		MAP_COLORS.put("color_pink", MapColor.COLOR_PINK);
		MAP_COLORS.put("color_gray", MapColor.COLOR_GRAY);
		MAP_COLORS.put("color_light_gray", MapColor.COLOR_LIGHT_GRAY);
		MAP_COLORS.put("color_cyan", MapColor.COLOR_CYAN);
		MAP_COLORS.put("color_purple", MapColor.COLOR_PURPLE);
		MAP_COLORS.put("color_blue", MapColor.COLOR_BLUE);
		MAP_COLORS.put("color_brown", MapColor.COLOR_BROWN);
		MAP_COLORS.put("color_green", MapColor.COLOR_GREEN);
		MAP_COLORS.put("color_red", MapColor.COLOR_RED);
		MAP_COLORS.put("color_black", MapColor.COLOR_BLACK);
		MAP_COLORS.put("gold", MapColor.GOLD);
		MAP_COLORS.put("diamond", MapColor.DIAMOND);
		MAP_COLORS.put("lapis", MapColor.LAPIS);
		MAP_COLORS.put("emerald", MapColor.EMERALD);
		MAP_COLORS.put("podzol", MapColor.PODZOL);
		MAP_COLORS.put("nether", MapColor.NETHER);
		MAP_COLORS.put("terracotta_white", MapColor.TERRACOTTA_WHITE);
		MAP_COLORS.put("terracotta_orange", MapColor.TERRACOTTA_ORANGE);
		MAP_COLORS.put("terracotta_magenta", MapColor.TERRACOTTA_MAGENTA);
		MAP_COLORS.put("terracotta_light_blue", MapColor.TERRACOTTA_LIGHT_BLUE);
		MAP_COLORS.put("terracotta_yellow", MapColor.TERRACOTTA_YELLOW);
		MAP_COLORS.put("terracotta_light_green", MapColor.TERRACOTTA_LIGHT_GREEN);
		MAP_COLORS.put("terracotta_pink", MapColor.TERRACOTTA_PINK);
		MAP_COLORS.put("terracotta_gray", MapColor.TERRACOTTA_GRAY);
		MAP_COLORS.put("terracotta_light_gray", MapColor.TERRACOTTA_LIGHT_GRAY);
		MAP_COLORS.put("terracotta_cyan", MapColor.TERRACOTTA_CYAN);
		MAP_COLORS.put("terracotta_purple", MapColor.TERRACOTTA_PURPLE);
		MAP_COLORS.put("terracotta_blue", MapColor.TERRACOTTA_BLUE);
		MAP_COLORS.put("terracotta_brown", MapColor.TERRACOTTA_BROWN);
		MAP_COLORS.put("terracotta_green", MapColor.TERRACOTTA_GREEN);
		MAP_COLORS.put("terracotta_red", MapColor.TERRACOTTA_RED);
		MAP_COLORS.put("terracotta_black", MapColor.TERRACOTTA_BLACK);
		MAP_COLORS.put("crimson_nylium", MapColor.CRIMSON_NYLIUM);
		MAP_COLORS.put("crimson_stem", MapColor.CRIMSON_STEM);
		MAP_COLORS.put("crimson_hyphae", MapColor.CRIMSON_HYPHAE);
		MAP_COLORS.put("warped_nylium", MapColor.WARPED_NYLIUM);
		MAP_COLORS.put("warped_stem", MapColor.WARPED_STEM);
		MAP_COLORS.put("warped_hyphae", MapColor.WARPED_HYPHAE);
		MAP_COLORS.put("warped_wart_block", MapColor.WARPED_WART_BLOCK);
		MAP_COLORS.put("deepslate", MapColor.DEEPSLATE);
		MAP_COLORS.put("raw_iron", MapColor.RAW_IRON);
		MAP_COLORS.put("glow_lichen", MapColor.GLOW_LICHEN);

		for (NoteBlockInstrument instrument : NoteBlockInstrument.values()) {
			if (instrument.isTunable()) {
				INSTRUMENTS.put(instrument.getSerializedName(), instrument);
			}
		}
	}

	public static <T> Codec<T> simpleByNameCodec(Map<Identifier, T> map) {
		return Identifier.CODEC.flatXmap(
				key -> {
					T value = map.get(key);
					if (value == null) {
						return DataResult.error(() -> "Unknown key: " + key);
					}
					return DataResult.success(value);
				}, _ -> {
					return DataResult.error(() -> "Unsupported operation");
				});
	}

	public static <T> Codec<T> simpleByNameCodec(BiMap<String, T> map) {
		return simpleByNameCodec(map, Codec.STRING);
	}

	public static <K, V> Codec<V> simpleByNameCodec(BiMap<K, V> map, Codec<K> keyCodec) {
		return keyCodec.flatXmap(
				key -> {
					V value = map.get(key);
					if (value == null) {
						return DataResult.error(() -> "Unknown key: " + key);
					}
					return DataResult.success(value);
				}, value -> {
					K key = map.inverse().get(value);
					if (key == null) {
						return DataResult.error(() -> "Unknown value: " + value);
					}
					return DataResult.success(key);
				});
	}

	public static <T> Codec<BlockBehaviour.StateArgumentPredicate<T>> stateArgumentPredicate() {
		return new Codec<>() {
			@Override
			public <R> DataResult<R> encode(BlockBehaviour.StateArgumentPredicate<T> input, DynamicOps<R> ops, R prefix) {
				return DataResult.error(() -> "Unsupported operation");
			}

			@Override
			public <R> DataResult<Pair<BlockBehaviour.StateArgumentPredicate<T>, R>> decode(DynamicOps<R> ops, R input) {
				Optional<Boolean> booleanValue = ops.getBooleanValue(input).result();
				if (booleanValue.isPresent()) {
					BlockBehaviour.StateArgumentPredicate<T> predicate = booleanValue.get() ?
							CustomizationCodecs::always :
							CustomizationCodecs::never;
					return DataResult.success(Pair.of(predicate, ops.empty()));
				}
				Optional<String> stringValue = ops.getStringValue(input).result();
				if (stringValue.isPresent()) {
					String s = stringValue.get();
					if ("ocelot_or_parrot".equals(s)) {
						return DataResult.success(Pair.of(
								(state, world, pos, entity) -> {
									return entity == EntityType.OCELOT || entity == EntityType.PARROT;
								}, ops.empty()));
					}
				}
				return DataResult.error(() -> "Failed to decode state argument predicate: " + input);
			}
		};
	}

	public static <T> boolean always(BlockState blockState, BlockGetter blockGetter, BlockPos pos, T t) {
		return true;
	}

	public static <T> boolean never(BlockState blockState, BlockGetter blockGetter, BlockPos pos, T t) {
		return false;
	}
}