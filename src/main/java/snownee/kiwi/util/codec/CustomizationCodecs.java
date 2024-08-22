package snownee.kiwi.util.codec;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import snownee.kiwi.RenderLayerEnum;
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
	public static final Codec<RenderLayerEnum> RENDER_TYPE = simpleByNameCodec(ImmutableBiMap.of(
			"cutout", RenderLayerEnum.CUTOUT,
			"cutout_mipped", RenderLayerEnum.CUTOUT_MIPPED,
			"translucent", RenderLayerEnum.TRANSLUCENT));
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
	public static final Codec<MinMaxBounds.Ints> INT_BOUNDS = MinMaxBounds.Ints.CODEC;
	// TODO BlockPredicate has its own Codec now.
	//  However, to use that, you need to wrap your JsonOps into RegistryOps, which requires a HolderLookup.Provider.
	//  Meaning, you need to get HolderLookup.Provider somewhere.
	public static final Codec<BlockPredicate> BLOCK_PREDICATE = new Codec<>() {
		@Override
		public <T> DataResult<Pair<BlockPredicate, T>> decode(DynamicOps<T> ops, T input) {
			String stringValue = ops.getStringValue(input).result().orElse(null);
			if (stringValue != null) {
				if (stringValue.startsWith("#")) {
					return DataResult.success(Pair.of(BlockPredicate.Builder.block()
							.of(TagKey.create(Registries.BLOCK, ResourceLocation.parse(stringValue.substring(1))))
							.build(), ops.empty()));
				}
				return DataResult.success(Pair.of(BlockPredicate.Builder.block()
						.of(BuiltInRegistries.BLOCK.get(ResourceLocation.parse(stringValue)))
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
	public static final BiMap<String, BlockSetType.PressurePlateSensitivity> SENSITIVITIES = HashBiMap.create();
	public static final Codec<BlockSetType.PressurePlateSensitivity> SENSITIVITY_CODEC = simpleByNameCodec(SENSITIVITIES);
	public static final Codec<WeatheringCopper.WeatherState> WEATHER_STATE = simpleByNameCodec(ImmutableBiMap.of(
			"unaffected", WeatheringCopper.WeatherState.UNAFFECTED,
			"exposed", WeatheringCopper.WeatherState.EXPOSED,
			"weathered", WeatheringCopper.WeatherState.WEATHERED,
			"oxidized", WeatheringCopper.WeatherState.OXIDIZED));

	public static final Codec<MobEffectInstance> MOB_EFFECT_INSTANCE = RecordCodecBuilder.create(instance -> instance.group(
			MobEffect.CODEC.fieldOf("id").forGetter(MobEffectInstance::getEffect),
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
			ExtraCodecs.NON_NEGATIVE_INT.fieldOf("nutrition").forGetter(FoodProperties::nutrition),
			Codec.FLOAT.fieldOf("saturation").forGetter(FoodProperties::saturation),
			Codec.BOOL.optionalFieldOf("can_always_eat", false).forGetter(FoodProperties::canAlwaysEat),
			Codec.FLOAT.fieldOf("eat_seconds").forGetter(FoodProperties::eatSeconds),
			ItemStack.OPTIONAL_CODEC.optionalFieldOf("using_converts_to").forGetter(FoodProperties::usingConvertsTo),
			FoodProperties.PossibleEffect.CODEC.listOf().optionalFieldOf("effects", List.of()).forGetter(FoodProperties::effects)
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
	/**
	 * @deprecated Use vanilla armor material registry
	 */
	@Deprecated
	public static final BiMap<ResourceLocation, ArmorMaterial> CUSTOM_ARMOR_MATERIALS = HashBiMap.create();
	public static final Codec<Holder<ArmorMaterial>> ARMOR_MATERIAL = ArmorMaterial.CODEC;

	static {
		// https://regex101.com/:
		// ^.+ ([a-zA-Z].+?) ([A-Z_]+) = new [a-zA-Z].+;
		// ->
		// SOUND_TYPES.put(new ResourceLocation("\L$2\E"), $1.$2);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("empty"), SoundType.EMPTY);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("wood"), SoundType.WOOD);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("gravel"), SoundType.GRAVEL);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("grass"), SoundType.GRASS);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("lily_pad"), SoundType.LILY_PAD);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("stone"), SoundType.STONE);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("metal"), SoundType.METAL);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("glass"), SoundType.GLASS);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("wool"), SoundType.WOOL);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("sand"), SoundType.SAND);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("snow"), SoundType.SNOW);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("powder_snow"), SoundType.POWDER_SNOW);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("ladder"), SoundType.LADDER);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("anvil"), SoundType.ANVIL);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("slime_block"), SoundType.SLIME_BLOCK);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("honey_block"), SoundType.HONEY_BLOCK);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("wet_grass"), SoundType.WET_GRASS);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("coral_block"), SoundType.CORAL_BLOCK);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("bamboo"), SoundType.BAMBOO);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("bamboo_sapling"), SoundType.BAMBOO_SAPLING);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("scaffolding"), SoundType.SCAFFOLDING);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("sweet_berry_bush"), SoundType.SWEET_BERRY_BUSH);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("crop"), SoundType.CROP);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("hard_crop"), SoundType.HARD_CROP);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("vine"), SoundType.VINE);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("nether_wart"), SoundType.NETHER_WART);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("lantern"), SoundType.LANTERN);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("stem"), SoundType.STEM);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("nylium"), SoundType.NYLIUM);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("fungus"), SoundType.FUNGUS);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("roots"), SoundType.ROOTS);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("shroomlight"), SoundType.SHROOMLIGHT);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("weeping_vines"), SoundType.WEEPING_VINES);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("twisting_vines"), SoundType.TWISTING_VINES);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("soul_sand"), SoundType.SOUL_SAND);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("soul_soil"), SoundType.SOUL_SOIL);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("basalt"), SoundType.BASALT);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("wart_block"), SoundType.WART_BLOCK);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("netherrack"), SoundType.NETHERRACK);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("nether_bricks"), SoundType.NETHER_BRICKS);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("nether_sprouts"), SoundType.NETHER_SPROUTS);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("nether_ore"), SoundType.NETHER_ORE);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("bone_block"), SoundType.BONE_BLOCK);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("netherite_block"), SoundType.NETHERITE_BLOCK);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("ancient_debris"), SoundType.ANCIENT_DEBRIS);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("lodestone"), SoundType.LODESTONE);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("chain"), SoundType.CHAIN);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("nether_gold_ore"), SoundType.NETHER_GOLD_ORE);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("gilded_blackstone"), SoundType.GILDED_BLACKSTONE);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("candle"), SoundType.CANDLE);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("amethyst"), SoundType.AMETHYST);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("amethyst_cluster"), SoundType.AMETHYST_CLUSTER);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("small_amethyst_bud"), SoundType.SMALL_AMETHYST_BUD);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("medium_amethyst_bud"), SoundType.MEDIUM_AMETHYST_BUD);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("large_amethyst_bud"), SoundType.LARGE_AMETHYST_BUD);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("tuff"), SoundType.TUFF);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("calcite"), SoundType.CALCITE);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("dripstone_block"), SoundType.DRIPSTONE_BLOCK);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("pointed_dripstone"), SoundType.POINTED_DRIPSTONE);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("copper"), SoundType.COPPER);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("cave_vines"), SoundType.CAVE_VINES);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("spore_blossom"), SoundType.SPORE_BLOSSOM);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("azalea"), SoundType.AZALEA);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("flowering_azalea"), SoundType.FLOWERING_AZALEA);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("moss_carpet"), SoundType.MOSS_CARPET);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("pink_petals"), SoundType.PINK_PETALS);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("moss"), SoundType.MOSS);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("big_dripleaf"), SoundType.BIG_DRIPLEAF);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("small_dripleaf"), SoundType.SMALL_DRIPLEAF);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("rooted_dirt"), SoundType.ROOTED_DIRT);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("hanging_roots"), SoundType.HANGING_ROOTS);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("azalea_leaves"), SoundType.AZALEA_LEAVES);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("sculk_sensor"), SoundType.SCULK_SENSOR);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("sculk_catalyst"), SoundType.SCULK_CATALYST);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("sculk"), SoundType.SCULK);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("sculk_vein"), SoundType.SCULK_VEIN);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("sculk_shrieker"), SoundType.SCULK_SHRIEKER);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("glow_lichen"), SoundType.GLOW_LICHEN);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("deepslate"), SoundType.DEEPSLATE);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("deepslate_bricks"), SoundType.DEEPSLATE_BRICKS);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("deepslate_tiles"), SoundType.DEEPSLATE_TILES);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("polished_deepslate"), SoundType.POLISHED_DEEPSLATE);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("froglight"), SoundType.FROGLIGHT);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("frogspawn"), SoundType.FROGSPAWN);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("mangrove_roots"), SoundType.MANGROVE_ROOTS);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("muddy_mangrove_roots"), SoundType.MUDDY_MANGROVE_ROOTS);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("mud"), SoundType.MUD);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("mud_bricks"), SoundType.MUD_BRICKS);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("packed_mud"), SoundType.PACKED_MUD);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("hanging_sign"), SoundType.HANGING_SIGN);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("nether_wood_hanging_sign"), SoundType.NETHER_WOOD_HANGING_SIGN);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("bamboo_wood_hanging_sign"), SoundType.BAMBOO_WOOD_HANGING_SIGN);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("bamboo_wood"), SoundType.BAMBOO_WOOD);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("nether_wood"), SoundType.NETHER_WOOD);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("cherry_wood"), SoundType.CHERRY_WOOD);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("cherry_sapling"), SoundType.CHERRY_SAPLING);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("cherry_leaves"), SoundType.CHERRY_LEAVES);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("cherry_wood_hanging_sign"), SoundType.CHERRY_WOOD_HANGING_SIGN);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("chiseled_bookshelf"), SoundType.CHISELED_BOOKSHELF);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("suspicious_sand"), SoundType.SUSPICIOUS_SAND);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("suspicious_gravel"), SoundType.SUSPICIOUS_GRAVEL);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("decorated_pot"), SoundType.DECORATED_POT);
		SOUND_TYPES.put(ResourceLocation.withDefaultNamespace("decorated_pot_cracked"), SoundType.DECORATED_POT_CRACKED);

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

		SENSITIVITIES.put("everything", BlockSetType.PressurePlateSensitivity.EVERYTHING);
		SENSITIVITIES.put("mobs", BlockSetType.PressurePlateSensitivity.MOBS);
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
}