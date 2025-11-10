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
		BUILTINS.put(new ResourceLocation("empty"), SoundType.EMPTY);
		BUILTINS.put(new ResourceLocation("wood"), SoundType.WOOD);
		BUILTINS.put(new ResourceLocation("gravel"), SoundType.GRAVEL);
		BUILTINS.put(new ResourceLocation("grass"), SoundType.GRASS);
		BUILTINS.put(new ResourceLocation("lily_pad"), SoundType.LILY_PAD);
		BUILTINS.put(new ResourceLocation("stone"), SoundType.STONE);
		BUILTINS.put(new ResourceLocation("metal"), SoundType.METAL);
		BUILTINS.put(new ResourceLocation("glass"), SoundType.GLASS);
		BUILTINS.put(new ResourceLocation("wool"), SoundType.WOOL);
		BUILTINS.put(new ResourceLocation("sand"), SoundType.SAND);
		BUILTINS.put(new ResourceLocation("snow"), SoundType.SNOW);
		BUILTINS.put(new ResourceLocation("powder_snow"), SoundType.POWDER_SNOW);
		BUILTINS.put(new ResourceLocation("ladder"), SoundType.LADDER);
		BUILTINS.put(new ResourceLocation("anvil"), SoundType.ANVIL);
		BUILTINS.put(new ResourceLocation("slime_block"), SoundType.SLIME_BLOCK);
		BUILTINS.put(new ResourceLocation("honey_block"), SoundType.HONEY_BLOCK);
		BUILTINS.put(new ResourceLocation("wet_grass"), SoundType.WET_GRASS);
		BUILTINS.put(new ResourceLocation("coral_block"), SoundType.CORAL_BLOCK);
		BUILTINS.put(new ResourceLocation("bamboo"), SoundType.BAMBOO);
		BUILTINS.put(new ResourceLocation("bamboo_sapling"), SoundType.BAMBOO_SAPLING);
		BUILTINS.put(new ResourceLocation("scaffolding"), SoundType.SCAFFOLDING);
		BUILTINS.put(new ResourceLocation("sweet_berry_bush"), SoundType.SWEET_BERRY_BUSH);
		BUILTINS.put(new ResourceLocation("crop"), SoundType.CROP);
		BUILTINS.put(new ResourceLocation("hard_crop"), SoundType.HARD_CROP);
		BUILTINS.put(new ResourceLocation("vine"), SoundType.VINE);
		BUILTINS.put(new ResourceLocation("nether_wart"), SoundType.NETHER_WART);
		BUILTINS.put(new ResourceLocation("lantern"), SoundType.LANTERN);
		BUILTINS.put(new ResourceLocation("stem"), SoundType.STEM);
		BUILTINS.put(new ResourceLocation("nylium"), SoundType.NYLIUM);
		BUILTINS.put(new ResourceLocation("fungus"), SoundType.FUNGUS);
		BUILTINS.put(new ResourceLocation("roots"), SoundType.ROOTS);
		BUILTINS.put(new ResourceLocation("shroomlight"), SoundType.SHROOMLIGHT);
		BUILTINS.put(new ResourceLocation("weeping_vines"), SoundType.WEEPING_VINES);
		BUILTINS.put(new ResourceLocation("twisting_vines"), SoundType.TWISTING_VINES);
		BUILTINS.put(new ResourceLocation("soul_sand"), SoundType.SOUL_SAND);
		BUILTINS.put(new ResourceLocation("soul_soil"), SoundType.SOUL_SOIL);
		BUILTINS.put(new ResourceLocation("basalt"), SoundType.BASALT);
		BUILTINS.put(new ResourceLocation("wart_block"), SoundType.WART_BLOCK);
		BUILTINS.put(new ResourceLocation("netherrack"), SoundType.NETHERRACK);
		BUILTINS.put(new ResourceLocation("nether_bricks"), SoundType.NETHER_BRICKS);
		BUILTINS.put(new ResourceLocation("nether_sprouts"), SoundType.NETHER_SPROUTS);
		BUILTINS.put(new ResourceLocation("nether_ore"), SoundType.NETHER_ORE);
		BUILTINS.put(new ResourceLocation("bone_block"), SoundType.BONE_BLOCK);
		BUILTINS.put(new ResourceLocation("netherite_block"), SoundType.NETHERITE_BLOCK);
		BUILTINS.put(new ResourceLocation("ancient_debris"), SoundType.ANCIENT_DEBRIS);
		BUILTINS.put(new ResourceLocation("lodestone"), SoundType.LODESTONE);
		BUILTINS.put(new ResourceLocation("chain"), SoundType.CHAIN);
		BUILTINS.put(new ResourceLocation("nether_gold_ore"), SoundType.NETHER_GOLD_ORE);
		BUILTINS.put(new ResourceLocation("gilded_blackstone"), SoundType.GILDED_BLACKSTONE);
		BUILTINS.put(new ResourceLocation("candle"), SoundType.CANDLE);
		BUILTINS.put(new ResourceLocation("amethyst"), SoundType.AMETHYST);
		BUILTINS.put(new ResourceLocation("amethyst_cluster"), SoundType.AMETHYST_CLUSTER);
		BUILTINS.put(new ResourceLocation("small_amethyst_bud"), SoundType.SMALL_AMETHYST_BUD);
		BUILTINS.put(new ResourceLocation("medium_amethyst_bud"), SoundType.MEDIUM_AMETHYST_BUD);
		BUILTINS.put(new ResourceLocation("large_amethyst_bud"), SoundType.LARGE_AMETHYST_BUD);
		BUILTINS.put(new ResourceLocation("tuff"), SoundType.TUFF);
		BUILTINS.put(new ResourceLocation("calcite"), SoundType.CALCITE);
		BUILTINS.put(new ResourceLocation("dripstone_block"), SoundType.DRIPSTONE_BLOCK);
		BUILTINS.put(new ResourceLocation("pointed_dripstone"), SoundType.POINTED_DRIPSTONE);
		BUILTINS.put(new ResourceLocation("copper"), SoundType.COPPER);
		BUILTINS.put(new ResourceLocation("cave_vines"), SoundType.CAVE_VINES);
		BUILTINS.put(new ResourceLocation("spore_blossom"), SoundType.SPORE_BLOSSOM);
		BUILTINS.put(new ResourceLocation("azalea"), SoundType.AZALEA);
		BUILTINS.put(new ResourceLocation("flowering_azalea"), SoundType.FLOWERING_AZALEA);
		BUILTINS.put(new ResourceLocation("moss_carpet"), SoundType.MOSS_CARPET);
		BUILTINS.put(new ResourceLocation("pink_petals"), SoundType.PINK_PETALS);
		BUILTINS.put(new ResourceLocation("moss"), SoundType.MOSS);
		BUILTINS.put(new ResourceLocation("big_dripleaf"), SoundType.BIG_DRIPLEAF);
		BUILTINS.put(new ResourceLocation("small_dripleaf"), SoundType.SMALL_DRIPLEAF);
		BUILTINS.put(new ResourceLocation("rooted_dirt"), SoundType.ROOTED_DIRT);
		BUILTINS.put(new ResourceLocation("hanging_roots"), SoundType.HANGING_ROOTS);
		BUILTINS.put(new ResourceLocation("azalea_leaves"), SoundType.AZALEA_LEAVES);
		BUILTINS.put(new ResourceLocation("sculk_sensor"), SoundType.SCULK_SENSOR);
		BUILTINS.put(new ResourceLocation("sculk_catalyst"), SoundType.SCULK_CATALYST);
		BUILTINS.put(new ResourceLocation("sculk"), SoundType.SCULK);
		BUILTINS.put(new ResourceLocation("sculk_vein"), SoundType.SCULK_VEIN);
		BUILTINS.put(new ResourceLocation("sculk_shrieker"), SoundType.SCULK_SHRIEKER);
		BUILTINS.put(new ResourceLocation("glow_lichen"), SoundType.GLOW_LICHEN);
		BUILTINS.put(new ResourceLocation("deepslate"), SoundType.DEEPSLATE);
		BUILTINS.put(new ResourceLocation("deepslate_bricks"), SoundType.DEEPSLATE_BRICKS);
		BUILTINS.put(new ResourceLocation("deepslate_tiles"), SoundType.DEEPSLATE_TILES);
		BUILTINS.put(new ResourceLocation("polished_deepslate"), SoundType.POLISHED_DEEPSLATE);
		BUILTINS.put(new ResourceLocation("froglight"), SoundType.FROGLIGHT);
		BUILTINS.put(new ResourceLocation("frogspawn"), SoundType.FROGSPAWN);
		BUILTINS.put(new ResourceLocation("mangrove_roots"), SoundType.MANGROVE_ROOTS);
		BUILTINS.put(new ResourceLocation("muddy_mangrove_roots"), SoundType.MUDDY_MANGROVE_ROOTS);
		BUILTINS.put(new ResourceLocation("mud"), SoundType.MUD);
		BUILTINS.put(new ResourceLocation("mud_bricks"), SoundType.MUD_BRICKS);
		BUILTINS.put(new ResourceLocation("packed_mud"), SoundType.PACKED_MUD);
		BUILTINS.put(new ResourceLocation("hanging_sign"), SoundType.HANGING_SIGN);
		BUILTINS.put(new ResourceLocation("nether_wood_hanging_sign"), SoundType.NETHER_WOOD_HANGING_SIGN);
		BUILTINS.put(new ResourceLocation("bamboo_wood_hanging_sign"), SoundType.BAMBOO_WOOD_HANGING_SIGN);
		BUILTINS.put(new ResourceLocation("bamboo_wood"), SoundType.BAMBOO_WOOD);
		BUILTINS.put(new ResourceLocation("nether_wood"), SoundType.NETHER_WOOD);
		BUILTINS.put(new ResourceLocation("cherry_wood"), SoundType.CHERRY_WOOD);
		BUILTINS.put(new ResourceLocation("cherry_sapling"), SoundType.CHERRY_SAPLING);
		BUILTINS.put(new ResourceLocation("cherry_leaves"), SoundType.CHERRY_LEAVES);
		BUILTINS.put(new ResourceLocation("cherry_wood_hanging_sign"), SoundType.CHERRY_WOOD_HANGING_SIGN);
		BUILTINS.put(new ResourceLocation("chiseled_bookshelf"), SoundType.CHISELED_BOOKSHELF);
		BUILTINS.put(new ResourceLocation("suspicious_sand"), SoundType.SUSPICIOUS_SAND);
		BUILTINS.put(new ResourceLocation("suspicious_gravel"), SoundType.SUSPICIOUS_GRAVEL);
		BUILTINS.put(new ResourceLocation("decorated_pot"), SoundType.DECORATED_POT);
		BUILTINS.put(new ResourceLocation("decorated_pot_cracked"), SoundType.DECORATED_POT_CRACKED);
	}

	public static void refreshWithValues(Map<ResourceLocation, ? extends SoundType> values) {
		ALL.clear();
		ALL.putAll(BUILTINS);
		ALL.putAll(values);
	}
}
