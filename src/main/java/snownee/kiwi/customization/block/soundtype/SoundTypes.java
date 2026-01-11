package snownee.kiwi.customization.block.soundtype;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SoundType;
import snownee.kiwi.util.codec.CustomizationCodecs;

public final class SoundTypes {
	public static final BiMap<ResourceLocation, SoundType> ALL = HashBiMap.create();
	public static final Map<ResourceLocation, SoundType> BUILTINS = new HashMap<>();
	public static final Codec<SoundType> CODEC = CustomizationCodecs.simpleByNameCodec(ALL);

	static {
		// https://regex101.com/:
		// ^.+ ([a-zA-Z].+?) ([A-Z_]+) = new [a-zA-Z].+;
		// ->
		// BUILTINS.put(new ResourceLocation("\L$2\E"), $1.$2);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("empty"), SoundType.EMPTY);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("wood"), SoundType.WOOD);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("gravel"), SoundType.GRAVEL);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("grass"), SoundType.GRASS);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("lily_pad"), SoundType.LILY_PAD);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("stone"), SoundType.STONE);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("metal"), SoundType.METAL);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("glass"), SoundType.GLASS);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("wool"), SoundType.WOOL);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("sand"), SoundType.SAND);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("snow"), SoundType.SNOW);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("powder_snow"), SoundType.POWDER_SNOW);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("ladder"), SoundType.LADDER);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("anvil"), SoundType.ANVIL);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("slime_block"), SoundType.SLIME_BLOCK);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("honey_block"), SoundType.HONEY_BLOCK);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("wet_grass"), SoundType.WET_GRASS);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("coral_block"), SoundType.CORAL_BLOCK);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("bamboo"), SoundType.BAMBOO);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("bamboo_sapling"), SoundType.BAMBOO_SAPLING);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("scaffolding"), SoundType.SCAFFOLDING);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("sweet_berry_bush"), SoundType.SWEET_BERRY_BUSH);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("crop"), SoundType.CROP);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("hard_crop"), SoundType.HARD_CROP);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("vine"), SoundType.VINE);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("nether_wart"), SoundType.NETHER_WART);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("lantern"), SoundType.LANTERN);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("stem"), SoundType.STEM);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("nylium"), SoundType.NYLIUM);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("fungus"), SoundType.FUNGUS);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("roots"), SoundType.ROOTS);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("shroomlight"), SoundType.SHROOMLIGHT);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("weeping_vines"), SoundType.WEEPING_VINES);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("twisting_vines"), SoundType.TWISTING_VINES);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("soul_sand"), SoundType.SOUL_SAND);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("soul_soil"), SoundType.SOUL_SOIL);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("basalt"), SoundType.BASALT);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("wart_block"), SoundType.WART_BLOCK);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("netherrack"), SoundType.NETHERRACK);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("nether_bricks"), SoundType.NETHER_BRICKS);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("nether_sprouts"), SoundType.NETHER_SPROUTS);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("nether_ore"), SoundType.NETHER_ORE);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("bone_block"), SoundType.BONE_BLOCK);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("netherite_block"), SoundType.NETHERITE_BLOCK);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("ancient_debris"), SoundType.ANCIENT_DEBRIS);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("lodestone"), SoundType.LODESTONE);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("chain"), SoundType.CHAIN);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("nether_gold_ore"), SoundType.NETHER_GOLD_ORE);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("gilded_blackstone"), SoundType.GILDED_BLACKSTONE);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("candle"), SoundType.CANDLE);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("amethyst"), SoundType.AMETHYST);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("amethyst_cluster"), SoundType.AMETHYST_CLUSTER);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("small_amethyst_bud"), SoundType.SMALL_AMETHYST_BUD);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("medium_amethyst_bud"), SoundType.MEDIUM_AMETHYST_BUD);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("large_amethyst_bud"), SoundType.LARGE_AMETHYST_BUD);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("tuff"), SoundType.TUFF);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("calcite"), SoundType.CALCITE);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("dripstone_block"), SoundType.DRIPSTONE_BLOCK);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("pointed_dripstone"), SoundType.POINTED_DRIPSTONE);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("copper"), SoundType.COPPER);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("cave_vines"), SoundType.CAVE_VINES);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("spore_blossom"), SoundType.SPORE_BLOSSOM);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("azalea"), SoundType.AZALEA);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("flowering_azalea"), SoundType.FLOWERING_AZALEA);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("moss_carpet"), SoundType.MOSS_CARPET);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("pink_petals"), SoundType.PINK_PETALS);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("moss"), SoundType.MOSS);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("big_dripleaf"), SoundType.BIG_DRIPLEAF);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("small_dripleaf"), SoundType.SMALL_DRIPLEAF);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("rooted_dirt"), SoundType.ROOTED_DIRT);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("hanging_roots"), SoundType.HANGING_ROOTS);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("azalea_leaves"), SoundType.AZALEA_LEAVES);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("sculk_sensor"), SoundType.SCULK_SENSOR);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("sculk_catalyst"), SoundType.SCULK_CATALYST);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("sculk"), SoundType.SCULK);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("sculk_vein"), SoundType.SCULK_VEIN);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("sculk_shrieker"), SoundType.SCULK_SHRIEKER);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("glow_lichen"), SoundType.GLOW_LICHEN);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("deepslate"), SoundType.DEEPSLATE);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("deepslate_bricks"), SoundType.DEEPSLATE_BRICKS);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("deepslate_tiles"), SoundType.DEEPSLATE_TILES);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("polished_deepslate"), SoundType.POLISHED_DEEPSLATE);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("froglight"), SoundType.FROGLIGHT);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("frogspawn"), SoundType.FROGSPAWN);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("mangrove_roots"), SoundType.MANGROVE_ROOTS);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("muddy_mangrove_roots"), SoundType.MUDDY_MANGROVE_ROOTS);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("mud"), SoundType.MUD);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("mud_bricks"), SoundType.MUD_BRICKS);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("packed_mud"), SoundType.PACKED_MUD);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("hanging_sign"), SoundType.HANGING_SIGN);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("nether_wood_hanging_sign"), SoundType.NETHER_WOOD_HANGING_SIGN);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("bamboo_wood_hanging_sign"), SoundType.BAMBOO_WOOD_HANGING_SIGN);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("bamboo_wood"), SoundType.BAMBOO_WOOD);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("nether_wood"), SoundType.NETHER_WOOD);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("cherry_wood"), SoundType.CHERRY_WOOD);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("cherry_sapling"), SoundType.CHERRY_SAPLING);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("cherry_leaves"), SoundType.CHERRY_LEAVES);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("cherry_wood_hanging_sign"), SoundType.CHERRY_WOOD_HANGING_SIGN);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("chiseled_bookshelf"), SoundType.CHISELED_BOOKSHELF);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("suspicious_sand"), SoundType.SUSPICIOUS_SAND);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("suspicious_gravel"), SoundType.SUSPICIOUS_GRAVEL);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("decorated_pot"), SoundType.DECORATED_POT);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("decorated_pot_cracked"), SoundType.DECORATED_POT_CRACKED);
	}

	public static void refreshWithValues(Map<ResourceLocation, ? extends SoundType> values) {
		ALL.clear();
		ALL.putAll(BUILTINS);
		ALL.putAll(values);
	}
}
