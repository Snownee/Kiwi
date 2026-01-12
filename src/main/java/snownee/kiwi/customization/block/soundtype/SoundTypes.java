package snownee.kiwi.customization.block.soundtype;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.SoundType;
import snownee.kiwi.util.codec.CustomizationCodecs;

public final class SoundTypes {
	public static final BiMap<Identifier, SoundType> ALL = HashBiMap.create();
	public static final Map<Identifier, SoundType> BUILTINS = new HashMap<>();
	public static final Codec<SoundType> CODEC = CustomizationCodecs.simpleByNameCodec(ALL);

	static {
		// https://regex101.com/:
		// ^.+ ([a-zA-Z].+?) ([A-Z_]+) = new [a-zA-Z].+;
		// ->
		// BUILTINS.put(new Identifier("\L$2\E"), $1.$2);
		BUILTINS.put(Identifier.withDefaultNamespace("empty"), SoundType.EMPTY);
		BUILTINS.put(Identifier.withDefaultNamespace("wood"), SoundType.WOOD);
		BUILTINS.put(Identifier.withDefaultNamespace("gravel"), SoundType.GRAVEL);
		BUILTINS.put(Identifier.withDefaultNamespace("grass"), SoundType.GRASS);
		BUILTINS.put(Identifier.withDefaultNamespace("lily_pad"), SoundType.LILY_PAD);
		BUILTINS.put(Identifier.withDefaultNamespace("stone"), SoundType.STONE);
		BUILTINS.put(Identifier.withDefaultNamespace("metal"), SoundType.METAL);
		BUILTINS.put(Identifier.withDefaultNamespace("glass"), SoundType.GLASS);
		BUILTINS.put(Identifier.withDefaultNamespace("wool"), SoundType.WOOL);
		BUILTINS.put(Identifier.withDefaultNamespace("sand"), SoundType.SAND);
		BUILTINS.put(Identifier.withDefaultNamespace("snow"), SoundType.SNOW);
		BUILTINS.put(Identifier.withDefaultNamespace("powder_snow"), SoundType.POWDER_SNOW);
		BUILTINS.put(Identifier.withDefaultNamespace("ladder"), SoundType.LADDER);
		BUILTINS.put(Identifier.withDefaultNamespace("anvil"), SoundType.ANVIL);
		BUILTINS.put(Identifier.withDefaultNamespace("slime_block"), SoundType.SLIME_BLOCK);
		BUILTINS.put(Identifier.withDefaultNamespace("honey_block"), SoundType.HONEY_BLOCK);
		BUILTINS.put(Identifier.withDefaultNamespace("wet_grass"), SoundType.WET_GRASS);
		BUILTINS.put(Identifier.withDefaultNamespace("coral_block"), SoundType.CORAL_BLOCK);
		BUILTINS.put(Identifier.withDefaultNamespace("bamboo"), SoundType.BAMBOO);
		BUILTINS.put(Identifier.withDefaultNamespace("bamboo_sapling"), SoundType.BAMBOO_SAPLING);
		BUILTINS.put(Identifier.withDefaultNamespace("scaffolding"), SoundType.SCAFFOLDING);
		BUILTINS.put(Identifier.withDefaultNamespace("sweet_berry_bush"), SoundType.SWEET_BERRY_BUSH);
		BUILTINS.put(Identifier.withDefaultNamespace("crop"), SoundType.CROP);
		BUILTINS.put(Identifier.withDefaultNamespace("hard_crop"), SoundType.HARD_CROP);
		BUILTINS.put(Identifier.withDefaultNamespace("vine"), SoundType.VINE);
		BUILTINS.put(Identifier.withDefaultNamespace("nether_wart"), SoundType.NETHER_WART);
		BUILTINS.put(Identifier.withDefaultNamespace("lantern"), SoundType.LANTERN);
		BUILTINS.put(Identifier.withDefaultNamespace("stem"), SoundType.STEM);
		BUILTINS.put(Identifier.withDefaultNamespace("nylium"), SoundType.NYLIUM);
		BUILTINS.put(Identifier.withDefaultNamespace("fungus"), SoundType.FUNGUS);
		BUILTINS.put(Identifier.withDefaultNamespace("roots"), SoundType.ROOTS);
		BUILTINS.put(Identifier.withDefaultNamespace("shroomlight"), SoundType.SHROOMLIGHT);
		BUILTINS.put(Identifier.withDefaultNamespace("weeping_vines"), SoundType.WEEPING_VINES);
		BUILTINS.put(Identifier.withDefaultNamespace("twisting_vines"), SoundType.TWISTING_VINES);
		BUILTINS.put(Identifier.withDefaultNamespace("soul_sand"), SoundType.SOUL_SAND);
		BUILTINS.put(Identifier.withDefaultNamespace("soul_soil"), SoundType.SOUL_SOIL);
		BUILTINS.put(Identifier.withDefaultNamespace("basalt"), SoundType.BASALT);
		BUILTINS.put(Identifier.withDefaultNamespace("wart_block"), SoundType.WART_BLOCK);
		BUILTINS.put(Identifier.withDefaultNamespace("netherrack"), SoundType.NETHERRACK);
		BUILTINS.put(Identifier.withDefaultNamespace("nether_bricks"), SoundType.NETHER_BRICKS);
		BUILTINS.put(Identifier.withDefaultNamespace("nether_sprouts"), SoundType.NETHER_SPROUTS);
		BUILTINS.put(Identifier.withDefaultNamespace("nether_ore"), SoundType.NETHER_ORE);
		BUILTINS.put(Identifier.withDefaultNamespace("bone_block"), SoundType.BONE_BLOCK);
		BUILTINS.put(Identifier.withDefaultNamespace("netherite_block"), SoundType.NETHERITE_BLOCK);
		BUILTINS.put(Identifier.withDefaultNamespace("ancient_debris"), SoundType.ANCIENT_DEBRIS);
		BUILTINS.put(Identifier.withDefaultNamespace("lodestone"), SoundType.LODESTONE);
		BUILTINS.put(Identifier.withDefaultNamespace("chain"), SoundType.CHAIN);
		BUILTINS.put(Identifier.withDefaultNamespace("nether_gold_ore"), SoundType.NETHER_GOLD_ORE);
		BUILTINS.put(Identifier.withDefaultNamespace("gilded_blackstone"), SoundType.GILDED_BLACKSTONE);
		BUILTINS.put(Identifier.withDefaultNamespace("candle"), SoundType.CANDLE);
		BUILTINS.put(Identifier.withDefaultNamespace("amethyst"), SoundType.AMETHYST);
		BUILTINS.put(Identifier.withDefaultNamespace("amethyst_cluster"), SoundType.AMETHYST_CLUSTER);
		BUILTINS.put(Identifier.withDefaultNamespace("small_amethyst_bud"), SoundType.SMALL_AMETHYST_BUD);
		BUILTINS.put(Identifier.withDefaultNamespace("medium_amethyst_bud"), SoundType.MEDIUM_AMETHYST_BUD);
		BUILTINS.put(Identifier.withDefaultNamespace("large_amethyst_bud"), SoundType.LARGE_AMETHYST_BUD);
		BUILTINS.put(Identifier.withDefaultNamespace("tuff"), SoundType.TUFF);
		BUILTINS.put(Identifier.withDefaultNamespace("calcite"), SoundType.CALCITE);
		BUILTINS.put(Identifier.withDefaultNamespace("dripstone_block"), SoundType.DRIPSTONE_BLOCK);
		BUILTINS.put(Identifier.withDefaultNamespace("pointed_dripstone"), SoundType.POINTED_DRIPSTONE);
		BUILTINS.put(Identifier.withDefaultNamespace("copper"), SoundType.COPPER);
		BUILTINS.put(Identifier.withDefaultNamespace("cave_vines"), SoundType.CAVE_VINES);
		BUILTINS.put(Identifier.withDefaultNamespace("spore_blossom"), SoundType.SPORE_BLOSSOM);
		BUILTINS.put(Identifier.withDefaultNamespace("azalea"), SoundType.AZALEA);
		BUILTINS.put(Identifier.withDefaultNamespace("flowering_azalea"), SoundType.FLOWERING_AZALEA);
		BUILTINS.put(Identifier.withDefaultNamespace("moss_carpet"), SoundType.MOSS_CARPET);
		BUILTINS.put(Identifier.withDefaultNamespace("pink_petals"), SoundType.PINK_PETALS);
		BUILTINS.put(Identifier.withDefaultNamespace("moss"), SoundType.MOSS);
		BUILTINS.put(Identifier.withDefaultNamespace("big_dripleaf"), SoundType.BIG_DRIPLEAF);
		BUILTINS.put(Identifier.withDefaultNamespace("small_dripleaf"), SoundType.SMALL_DRIPLEAF);
		BUILTINS.put(Identifier.withDefaultNamespace("rooted_dirt"), SoundType.ROOTED_DIRT);
		BUILTINS.put(Identifier.withDefaultNamespace("hanging_roots"), SoundType.HANGING_ROOTS);
		BUILTINS.put(Identifier.withDefaultNamespace("azalea_leaves"), SoundType.AZALEA_LEAVES);
		BUILTINS.put(Identifier.withDefaultNamespace("sculk_sensor"), SoundType.SCULK_SENSOR);
		BUILTINS.put(Identifier.withDefaultNamespace("sculk_catalyst"), SoundType.SCULK_CATALYST);
		BUILTINS.put(Identifier.withDefaultNamespace("sculk"), SoundType.SCULK);
		BUILTINS.put(Identifier.withDefaultNamespace("sculk_vein"), SoundType.SCULK_VEIN);
		BUILTINS.put(Identifier.withDefaultNamespace("sculk_shrieker"), SoundType.SCULK_SHRIEKER);
		BUILTINS.put(Identifier.withDefaultNamespace("glow_lichen"), SoundType.GLOW_LICHEN);
		BUILTINS.put(Identifier.withDefaultNamespace("deepslate"), SoundType.DEEPSLATE);
		BUILTINS.put(Identifier.withDefaultNamespace("deepslate_bricks"), SoundType.DEEPSLATE_BRICKS);
		BUILTINS.put(Identifier.withDefaultNamespace("deepslate_tiles"), SoundType.DEEPSLATE_TILES);
		BUILTINS.put(Identifier.withDefaultNamespace("polished_deepslate"), SoundType.POLISHED_DEEPSLATE);
		BUILTINS.put(Identifier.withDefaultNamespace("froglight"), SoundType.FROGLIGHT);
		BUILTINS.put(Identifier.withDefaultNamespace("frogspawn"), SoundType.FROGSPAWN);
		BUILTINS.put(Identifier.withDefaultNamespace("mangrove_roots"), SoundType.MANGROVE_ROOTS);
		BUILTINS.put(Identifier.withDefaultNamespace("muddy_mangrove_roots"), SoundType.MUDDY_MANGROVE_ROOTS);
		BUILTINS.put(Identifier.withDefaultNamespace("mud"), SoundType.MUD);
		BUILTINS.put(Identifier.withDefaultNamespace("mud_bricks"), SoundType.MUD_BRICKS);
		BUILTINS.put(Identifier.withDefaultNamespace("packed_mud"), SoundType.PACKED_MUD);
		BUILTINS.put(Identifier.withDefaultNamespace("hanging_sign"), SoundType.HANGING_SIGN);
		BUILTINS.put(Identifier.withDefaultNamespace("nether_wood_hanging_sign"), SoundType.NETHER_WOOD_HANGING_SIGN);
		BUILTINS.put(Identifier.withDefaultNamespace("bamboo_wood_hanging_sign"), SoundType.BAMBOO_WOOD_HANGING_SIGN);
		BUILTINS.put(Identifier.withDefaultNamespace("bamboo_wood"), SoundType.BAMBOO_WOOD);
		BUILTINS.put(Identifier.withDefaultNamespace("nether_wood"), SoundType.NETHER_WOOD);
		BUILTINS.put(Identifier.withDefaultNamespace("cherry_wood"), SoundType.CHERRY_WOOD);
		BUILTINS.put(Identifier.withDefaultNamespace("cherry_sapling"), SoundType.CHERRY_SAPLING);
		BUILTINS.put(Identifier.withDefaultNamespace("cherry_leaves"), SoundType.CHERRY_LEAVES);
		BUILTINS.put(Identifier.withDefaultNamespace("cherry_wood_hanging_sign"), SoundType.CHERRY_WOOD_HANGING_SIGN);
		BUILTINS.put(Identifier.withDefaultNamespace("chiseled_bookshelf"), SoundType.CHISELED_BOOKSHELF);
		BUILTINS.put(Identifier.withDefaultNamespace("suspicious_sand"), SoundType.SUSPICIOUS_SAND);
		BUILTINS.put(Identifier.withDefaultNamespace("suspicious_gravel"), SoundType.SUSPICIOUS_GRAVEL);
		BUILTINS.put(Identifier.withDefaultNamespace("decorated_pot"), SoundType.DECORATED_POT);
		BUILTINS.put(Identifier.withDefaultNamespace("decorated_pot_cracked"), SoundType.DECORATED_POT_CRACKED);
	}

	public static void refreshWithValues(Map<Identifier, ? extends SoundType> values) {
		ALL.clear();
		ALL.putAll(BUILTINS);
		ALL.putAll(values);
	}
}
