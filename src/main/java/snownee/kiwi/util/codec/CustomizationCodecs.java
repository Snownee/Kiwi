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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.customization.block.GlassType;

public class CustomizationCodecs {
	public static final BiMap<ResourceLocation, SoundType> SOUND_TYPES = HashBiMap.create();
	public static final Codec<SoundType> SOUND_TYPE_CODEC = simpleByNameCodec(SOUND_TYPES);
	public static final BiMap<String, NoteBlockInstrument> INSTRUMENTS = HashBiMap.create();
	public static final Codec<NoteBlockInstrument> INSTRUMENT_CODEC = simpleByNameCodec(INSTRUMENTS);
	public static final BiMap<String, MapColor> MAP_COLORS = HashBiMap.create();
	public static final Codec<MapColor> MAP_COLOR_CODEC = simpleByNameCodec(MAP_COLORS);
	public static final BiMap<ResourceLocation, GlassType> GLASS_TYPES = HashBiMap.create();
	public static final Codec<GlassType> GLASS_TYPE_CODEC = simpleByNameCodec(GLASS_TYPES);
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
	public static final Codec<BlockBehaviour.StatePredicate> STATE_PREDICATE = Codec.BOOL.flatComapMap(bl -> {
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
					return DataResult.success(Pair.of(BlockPredicate.Builder.block()
							.of(TagKey.create(Registries.BLOCK, new ResourceLocation(stringValue.substring(1))))
							.build(), ops.empty()));
				}
				return DataResult.success(Pair.of(BlockPredicate.Builder.block()
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
		// https://regex101.com/:
		// ^.+ ([a-zA-Z].+?) ([A-Z_]+) = new [a-zA-Z].+;
		// ->
		// SOUND_TYPES.put(new ResourceLocation("\L$2\E"), $1.$2);
		SOUND_TYPES.put(new ResourceLocation("empty"), SoundType.EMPTY);
		SOUND_TYPES.put(new ResourceLocation("wood"), SoundType.WOOD);
		SOUND_TYPES.put(new ResourceLocation("gravel"), SoundType.GRAVEL);
		SOUND_TYPES.put(new ResourceLocation("grass"), SoundType.GRASS);
		SOUND_TYPES.put(new ResourceLocation("lily_pad"), SoundType.LILY_PAD);
		SOUND_TYPES.put(new ResourceLocation("stone"), SoundType.STONE);
		SOUND_TYPES.put(new ResourceLocation("metal"), SoundType.METAL);
		SOUND_TYPES.put(new ResourceLocation("glass"), SoundType.GLASS);
		SOUND_TYPES.put(new ResourceLocation("wool"), SoundType.WOOL);
		SOUND_TYPES.put(new ResourceLocation("sand"), SoundType.SAND);
		SOUND_TYPES.put(new ResourceLocation("snow"), SoundType.SNOW);
		SOUND_TYPES.put(new ResourceLocation("powder_snow"), SoundType.POWDER_SNOW);
		SOUND_TYPES.put(new ResourceLocation("ladder"), SoundType.LADDER);
		SOUND_TYPES.put(new ResourceLocation("anvil"), SoundType.ANVIL);
		SOUND_TYPES.put(new ResourceLocation("slime_block"), SoundType.SLIME_BLOCK);
		SOUND_TYPES.put(new ResourceLocation("honey_block"), SoundType.HONEY_BLOCK);
		SOUND_TYPES.put(new ResourceLocation("wet_grass"), SoundType.WET_GRASS);
		SOUND_TYPES.put(new ResourceLocation("coral_block"), SoundType.CORAL_BLOCK);
		SOUND_TYPES.put(new ResourceLocation("bamboo"), SoundType.BAMBOO);
		SOUND_TYPES.put(new ResourceLocation("bamboo_sapling"), SoundType.BAMBOO_SAPLING);
		SOUND_TYPES.put(new ResourceLocation("scaffolding"), SoundType.SCAFFOLDING);
		SOUND_TYPES.put(new ResourceLocation("sweet_berry_bush"), SoundType.SWEET_BERRY_BUSH);
		SOUND_TYPES.put(new ResourceLocation("crop"), SoundType.CROP);
		SOUND_TYPES.put(new ResourceLocation("hard_crop"), SoundType.HARD_CROP);
		SOUND_TYPES.put(new ResourceLocation("vine"), SoundType.VINE);
		SOUND_TYPES.put(new ResourceLocation("nether_wart"), SoundType.NETHER_WART);
		SOUND_TYPES.put(new ResourceLocation("lantern"), SoundType.LANTERN);
		SOUND_TYPES.put(new ResourceLocation("stem"), SoundType.STEM);
		SOUND_TYPES.put(new ResourceLocation("nylium"), SoundType.NYLIUM);
		SOUND_TYPES.put(new ResourceLocation("fungus"), SoundType.FUNGUS);
		SOUND_TYPES.put(new ResourceLocation("roots"), SoundType.ROOTS);
		SOUND_TYPES.put(new ResourceLocation("shroomlight"), SoundType.SHROOMLIGHT);
		SOUND_TYPES.put(new ResourceLocation("weeping_vines"), SoundType.WEEPING_VINES);
		SOUND_TYPES.put(new ResourceLocation("twisting_vines"), SoundType.TWISTING_VINES);
		SOUND_TYPES.put(new ResourceLocation("soul_sand"), SoundType.SOUL_SAND);
		SOUND_TYPES.put(new ResourceLocation("soul_soil"), SoundType.SOUL_SOIL);
		SOUND_TYPES.put(new ResourceLocation("basalt"), SoundType.BASALT);
		SOUND_TYPES.put(new ResourceLocation("wart_block"), SoundType.WART_BLOCK);
		SOUND_TYPES.put(new ResourceLocation("netherrack"), SoundType.NETHERRACK);
		SOUND_TYPES.put(new ResourceLocation("nether_bricks"), SoundType.NETHER_BRICKS);
		SOUND_TYPES.put(new ResourceLocation("nether_sprouts"), SoundType.NETHER_SPROUTS);
		SOUND_TYPES.put(new ResourceLocation("nether_ore"), SoundType.NETHER_ORE);
		SOUND_TYPES.put(new ResourceLocation("bone_block"), SoundType.BONE_BLOCK);
		SOUND_TYPES.put(new ResourceLocation("netherite_block"), SoundType.NETHERITE_BLOCK);
		SOUND_TYPES.put(new ResourceLocation("ancient_debris"), SoundType.ANCIENT_DEBRIS);
		SOUND_TYPES.put(new ResourceLocation("lodestone"), SoundType.LODESTONE);
		SOUND_TYPES.put(new ResourceLocation("chain"), SoundType.CHAIN);
		SOUND_TYPES.put(new ResourceLocation("nether_gold_ore"), SoundType.NETHER_GOLD_ORE);
		SOUND_TYPES.put(new ResourceLocation("gilded_blackstone"), SoundType.GILDED_BLACKSTONE);
		SOUND_TYPES.put(new ResourceLocation("candle"), SoundType.CANDLE);
		SOUND_TYPES.put(new ResourceLocation("amethyst"), SoundType.AMETHYST);
		SOUND_TYPES.put(new ResourceLocation("amethyst_cluster"), SoundType.AMETHYST_CLUSTER);
		SOUND_TYPES.put(new ResourceLocation("small_amethyst_bud"), SoundType.SMALL_AMETHYST_BUD);
		SOUND_TYPES.put(new ResourceLocation("medium_amethyst_bud"), SoundType.MEDIUM_AMETHYST_BUD);
		SOUND_TYPES.put(new ResourceLocation("large_amethyst_bud"), SoundType.LARGE_AMETHYST_BUD);
		SOUND_TYPES.put(new ResourceLocation("tuff"), SoundType.TUFF);
		SOUND_TYPES.put(new ResourceLocation("calcite"), SoundType.CALCITE);
		SOUND_TYPES.put(new ResourceLocation("dripstone_block"), SoundType.DRIPSTONE_BLOCK);
		SOUND_TYPES.put(new ResourceLocation("pointed_dripstone"), SoundType.POINTED_DRIPSTONE);
		SOUND_TYPES.put(new ResourceLocation("copper"), SoundType.COPPER);
		SOUND_TYPES.put(new ResourceLocation("cave_vines"), SoundType.CAVE_VINES);
		SOUND_TYPES.put(new ResourceLocation("spore_blossom"), SoundType.SPORE_BLOSSOM);
		SOUND_TYPES.put(new ResourceLocation("azalea"), SoundType.AZALEA);
		SOUND_TYPES.put(new ResourceLocation("flowering_azalea"), SoundType.FLOWERING_AZALEA);
		SOUND_TYPES.put(new ResourceLocation("moss_carpet"), SoundType.MOSS_CARPET);
		SOUND_TYPES.put(new ResourceLocation("pink_petals"), SoundType.PINK_PETALS);
		SOUND_TYPES.put(new ResourceLocation("moss"), SoundType.MOSS);
		SOUND_TYPES.put(new ResourceLocation("big_dripleaf"), SoundType.BIG_DRIPLEAF);
		SOUND_TYPES.put(new ResourceLocation("small_dripleaf"), SoundType.SMALL_DRIPLEAF);
		SOUND_TYPES.put(new ResourceLocation("rooted_dirt"), SoundType.ROOTED_DIRT);
		SOUND_TYPES.put(new ResourceLocation("hanging_roots"), SoundType.HANGING_ROOTS);
		SOUND_TYPES.put(new ResourceLocation("azalea_leaves"), SoundType.AZALEA_LEAVES);
		SOUND_TYPES.put(new ResourceLocation("sculk_sensor"), SoundType.SCULK_SENSOR);
		SOUND_TYPES.put(new ResourceLocation("sculk_catalyst"), SoundType.SCULK_CATALYST);
		SOUND_TYPES.put(new ResourceLocation("sculk"), SoundType.SCULK);
		SOUND_TYPES.put(new ResourceLocation("sculk_vein"), SoundType.SCULK_VEIN);
		SOUND_TYPES.put(new ResourceLocation("sculk_shrieker"), SoundType.SCULK_SHRIEKER);
		SOUND_TYPES.put(new ResourceLocation("glow_lichen"), SoundType.GLOW_LICHEN);
		SOUND_TYPES.put(new ResourceLocation("deepslate"), SoundType.DEEPSLATE);
		SOUND_TYPES.put(new ResourceLocation("deepslate_bricks"), SoundType.DEEPSLATE_BRICKS);
		SOUND_TYPES.put(new ResourceLocation("deepslate_tiles"), SoundType.DEEPSLATE_TILES);
		SOUND_TYPES.put(new ResourceLocation("polished_deepslate"), SoundType.POLISHED_DEEPSLATE);
		SOUND_TYPES.put(new ResourceLocation("froglight"), SoundType.FROGLIGHT);
		SOUND_TYPES.put(new ResourceLocation("frogspawn"), SoundType.FROGSPAWN);
		SOUND_TYPES.put(new ResourceLocation("mangrove_roots"), SoundType.MANGROVE_ROOTS);
		SOUND_TYPES.put(new ResourceLocation("muddy_mangrove_roots"), SoundType.MUDDY_MANGROVE_ROOTS);
		SOUND_TYPES.put(new ResourceLocation("mud"), SoundType.MUD);
		SOUND_TYPES.put(new ResourceLocation("mud_bricks"), SoundType.MUD_BRICKS);
		SOUND_TYPES.put(new ResourceLocation("packed_mud"), SoundType.PACKED_MUD);
		SOUND_TYPES.put(new ResourceLocation("hanging_sign"), SoundType.HANGING_SIGN);
		SOUND_TYPES.put(new ResourceLocation("nether_wood_hanging_sign"), SoundType.NETHER_WOOD_HANGING_SIGN);
		SOUND_TYPES.put(new ResourceLocation("bamboo_wood_hanging_sign"), SoundType.BAMBOO_WOOD_HANGING_SIGN);
		SOUND_TYPES.put(new ResourceLocation("bamboo_wood"), SoundType.BAMBOO_WOOD);
		SOUND_TYPES.put(new ResourceLocation("nether_wood"), SoundType.NETHER_WOOD);
		SOUND_TYPES.put(new ResourceLocation("cherry_wood"), SoundType.CHERRY_WOOD);
		SOUND_TYPES.put(new ResourceLocation("cherry_sapling"), SoundType.CHERRY_SAPLING);
		SOUND_TYPES.put(new ResourceLocation("cherry_leaves"), SoundType.CHERRY_LEAVES);
		SOUND_TYPES.put(new ResourceLocation("cherry_wood_hanging_sign"), SoundType.CHERRY_WOOD_HANGING_SIGN);
		SOUND_TYPES.put(new ResourceLocation("chiseled_bookshelf"), SoundType.CHISELED_BOOKSHELF);
		SOUND_TYPES.put(new ResourceLocation("suspicious_sand"), SoundType.SUSPICIOUS_SAND);
		SOUND_TYPES.put(new ResourceLocation("suspicious_gravel"), SoundType.SUSPICIOUS_GRAVEL);
		SOUND_TYPES.put(new ResourceLocation("decorated_pot"), SoundType.DECORATED_POT);
		SOUND_TYPES.put(new ResourceLocation("decorated_pot_cracked"), SoundType.DECORATED_POT_CRACKED);

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

		Objects.requireNonNull(GlassType.CLEAR);

		SENSITIVITIES.put("everything", PressurePlateBlock.Sensitivity.EVERYTHING);
		SENSITIVITIES.put("mobs", PressurePlateBlock.Sensitivity.MOBS);
	}

	public static <T> Codec<T> simpleByNameCodec(Map<ResourceLocation, T> map) {
		return ResourceLocation.CODEC.flatXmap(key -> {
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
		return keyCodec.flatXmap(key -> {
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
						return DataResult.success(Pair.of((state, world, pos, entity) -> {
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
