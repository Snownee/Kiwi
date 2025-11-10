package snownee.kiwi.util.codec;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import snownee.kiwi.KiwiModule;

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
	public static final Codec<KiwiModule.RenderLayer.Layer> RENDER_TYPE = simpleByNameCodec(ImmutableBiMap.of(
			"cutout", KiwiModule.RenderLayer.Layer.CUTOUT,
			"cutout_mipped", KiwiModule.RenderLayer.Layer.CUTOUT_MIPPED,
			"translucent", KiwiModule.RenderLayer.Layer.TRANSLUCENT));
	public static final Codec<BlockBehaviour.OffsetType> OFFSET_TYPE = simpleByNameCodec(ImmutableBiMap.of(
			"xz", BlockBehaviour.OffsetType.XZ,
			"xyz", BlockBehaviour.OffsetType.XYZ));
	public static final Codec<BlockBehaviour.StatePredicate> STATE_PREDICATE = Codec.BOOL.flatComapMap(
			bl -> {
				return bl ? Blocks::always : Blocks::never;
			}, p -> {
				return DataResult.error(() -> "Unsupported operation");
			});
	public static final Codec<Direction> DIRECTION = simpleByNameCodec(ImmutableBiMap.of(
			"down", Direction.DOWN,
			"up", Direction.UP,
			"north", Direction.NORTH,
			"south", Direction.SOUTH,
			"west", Direction.WEST,
			"east", Direction.EAST));
	public static final Codec<MinMaxBounds.Ints> INT_BOUNDS = ExtraCodecs.JSON.xmap(
			MinMaxBounds.Ints::fromJson,
			MinMaxBounds::serializeToJson);
	public static final Codec<BlockPredicate> BLOCK_PREDICATE = new Codec<>() {
		@Override
		public <T> DataResult<Pair<BlockPredicate, T>> decode(DynamicOps<T> ops, T input) {
			String stringValue = ops.getStringValue(input).result().orElse(null);
			if (stringValue != null) {
				if (stringValue.startsWith("#")) {
					return DataResult.success(Pair.of(
							BlockPredicate.Builder.block()
									.of(TagKey.create(Registries.BLOCK, new ResourceLocation(stringValue.substring(1))))
									.build(), ops.empty()));
				}
				return DataResult.success(Pair.of(
						BlockPredicate.Builder.block()
								.of(BuiltInRegistries.BLOCK.get(new ResourceLocation(stringValue)))
								.build(), ops.empty()));
			}
			return ExtraCodecs.JSON.decode(ops, input).map($ -> $.mapFirst(BlockPredicate::fromJson));
		}

		@Override
		public <T> DataResult<T> encode(BlockPredicate input, DynamicOps<T> ops, T prefix) {
			return ExtraCodecs.JSON.encodeStart(ops, input.serializeToJson());
		}
	};
	public static final Codec<BlockSetType> BLOCK_SET_TYPE = ExtraCodecs.stringResolverCodec(
			BlockSetType::name,
			s -> BlockSetType.values().filter(e -> e.name().equals(s)).findFirst().orElseThrow());
	public static final Codec<WoodType> WOOD_TYPE = ExtraCodecs.stringResolverCodec(
			WoodType::name,
			s -> WoodType.values().filter(e -> e.name().equals(s)).findFirst().orElseThrow());
	public static final BiMap<String, PressurePlateBlock.Sensitivity> SENSITIVITIES = HashBiMap.create();
	public static final Codec<PressurePlateBlock.Sensitivity> SENSITIVITY_CODEC = simpleByNameCodec(SENSITIVITIES);
	public static final Codec<WeatheringCopper.WeatherState> WEATHER_STATE = simpleByNameCodec(ImmutableBiMap.of(
			"unaffected", WeatheringCopper.WeatherState.UNAFFECTED,
			"exposed", WeatheringCopper.WeatherState.EXPOSED,
			"weathered", WeatheringCopper.WeatherState.WEATHERED,
			"oxidized", WeatheringCopper.WeatherState.OXIDIZED));

	public static final Codec<MobEffectInstance> MOB_EFFECT_INSTANCE = RecordCodecBuilder.create(instance -> instance.group(
			BuiltInRegistries.MOB_EFFECT.byNameCodec().fieldOf("id").forGetter(MobEffectInstance::getEffect),
			ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("amplifier", 0).forGetter(MobEffectInstance::getAmplifier),
			Codec.INT.optionalFieldOf("duration", 0).forGetter(MobEffectInstance::getDuration),
			Codec.BOOL.optionalFieldOf("ambient", false).forGetter(MobEffectInstance::isAmbient),
			Codec.BOOL.optionalFieldOf("show_particles", true).forGetter(MobEffectInstance::isVisible),
			Codec.BOOL.optionalFieldOf("show_icon", true).forGetter(MobEffectInstance::showIcon)
	).apply(instance, MobEffectInstance::new));

	public static final Codec<Pair<MobEffectInstance, Float>> POSSIBLE_EFFECT = RecordCodecBuilder.create(instance -> instance.group(
			MOB_EFFECT_INSTANCE.fieldOf("effect").forGetter(Pair::getFirst),
			Codec.floatRange(0.0f, 1.0f).optionalFieldOf("probability", 1F).forGetter(Pair::getSecond)
	).apply(instance, Pair::of));

	public static final Codec<FoodProperties> FOOD = RecordCodecBuilder.create(instance -> instance.group(
			ExtraCodecs.NON_NEGATIVE_INT.fieldOf("nutrition").forGetter(FoodProperties::getNutrition),
			Codec.FLOAT.fieldOf("saturation").forGetter(FoodProperties::getSaturationModifier),
			Codec.BOOL.optionalFieldOf("meat", false).forGetter(FoodProperties::isMeat),
			Codec.BOOL.optionalFieldOf("can_always_eat", false).forGetter(FoodProperties::canAlwaysEat),
			Codec.BOOL.optionalFieldOf("fast_food", false).forGetter(FoodProperties::isFastFood),
			POSSIBLE_EFFECT.listOf().optionalFieldOf("effects", List.of()).forGetter(FoodProperties::getEffects)
	).apply(instance, FoodProperties::new));

	public static final BiMap<String, Rarity> RARITIES = HashBiMap.create(Map.of(
			"common", Rarity.COMMON,
			"uncommon", Rarity.UNCOMMON,
			"rare", Rarity.RARE,
			"epic", Rarity.EPIC));
	public static final Codec<Rarity> RARITY_CODEC = simpleByNameCodec(RARITIES);
	public static final Codec<ArmorItem.Type> ARMOR_TYPE = simpleByNameCodec(ImmutableBiMap.of(
			"helmet", ArmorItem.Type.HELMET,
			"chestplate", ArmorItem.Type.CHESTPLATE,
			"leggings", ArmorItem.Type.LEGGINGS,
			"boots", ArmorItem.Type.BOOTS));
	public static final BiMap<ResourceLocation, ArmorMaterial> CUSTOM_ARMOR_MATERIALS = HashBiMap.create();
	@SuppressWarnings("unchecked")
	public static final Codec<ArmorMaterial> ARMOR_MATERIAL = CustomizationCodecs.withAlternative(
			(Codec<ArmorMaterial>) (Object) StringRepresentable.fromEnum(ArmorMaterials::values),
			simpleByNameCodec(CUSTOM_ARMOR_MATERIALS));

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

		SENSITIVITIES.put("everything", PressurePlateBlock.Sensitivity.EVERYTHING);
		SENSITIVITIES.put("mobs", PressurePlateBlock.Sensitivity.MOBS);
	}

	public static <T> Codec<T> simpleByNameCodec(Map<ResourceLocation, T> map) {
		return ResourceLocation.CODEC.flatXmap(
				key -> {
					T value = map.get(key);
					if (value == null) {
						return DataResult.error(() -> "Unknown key: " + key);
					}
					return DataResult.success(value);
				}, value -> {
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

	public static <T> Codec<T> withAlternative(Codec<T> codec, Codec<? extends T> codec2) {
		return new ExtraCodecs.EitherCodec<>(codec, codec2).xmap(either -> either.map(object -> object, object -> object), Either::left);
	}

	public static <T> Codec<List<T>> compactList(Codec<T> codec) {
		return withAlternative(codec.listOf(), codec.xmap(List::of, list -> list.get(0)));
	}

	public static <A> MapCodec<Optional<A>> strictOptionalField(Codec<A> codec, String string) {
		return new StrictOptionalFieldCodec<>(string, codec);
	}

	public static <A> MapCodec<A> strictOptionalField(Codec<A> codec, String string, A object) {
		return strictOptionalField(codec, string).xmap(
				optional -> optional.orElse(object),
				object2 -> Objects.equals(object2, object) ? Optional.empty() : Optional.of(object2));
	}

	static final class StrictOptionalFieldCodec<A>
			extends MapCodec<Optional<A>> {
		private final String name;
		private final Codec<A> elementCodec;

		public StrictOptionalFieldCodec(String string, Codec<A> codec) {
			this.name = string;
			this.elementCodec = codec;
		}

		@Override
		public <T> DataResult<Optional<A>> decode(DynamicOps<T> dynamicOps, MapLike<T> mapLike) {
			T object = mapLike.get(this.name);
			if (object == null) {
				return DataResult.success(Optional.empty());
			}
			return this.elementCodec.parse(dynamicOps, object).map(Optional::of);
		}

		@Override
		public <T> RecordBuilder<T> encode(Optional<A> optional, DynamicOps<T> dynamicOps, RecordBuilder<T> recordBuilder) {
			if (optional.isPresent()) {
				return recordBuilder.add(this.name, this.elementCodec.encodeStart(dynamicOps, optional.get()));
			}
			return recordBuilder;
		}

		@Override
		public <T> Stream<T> keys(DynamicOps<T> dynamicOps) {
			return Stream.of(dynamicOps.createString(this.name));
		}

		public boolean equals(Object object) {
			if (this == object) {
				return true;
			}
			if (object instanceof StrictOptionalFieldCodec<?> strictOptionalFieldCodec) {
				return Objects.equals(this.name, strictOptionalFieldCodec.name) && Objects.equals(
						this.elementCodec,
						strictOptionalFieldCodec.elementCodec);
			}
			return false;
		}

		public int hashCode() {
			return Objects.hash(this.name, this.elementCodec);
		}

		public String toString() {
			return "StrictOptionalFieldCodec[" + this.name + ": " + this.elementCodec + "]";
		}
	}
}
