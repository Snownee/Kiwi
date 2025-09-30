package snownee.kiwi.customization.block.tier;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import snownee.kiwi.util.codec.CustomizationCodecs;

public final class KiwiTiers {
	public static final BiMap<ResourceLocation, Tier> ALL = HashBiMap.create();
	public static final Map<ResourceLocation, Tier> BUILTINS = new HashMap<>();
	public static final Codec<Tier> CODEC = CustomizationCodecs.simpleByNameCodec(ALL);

	static {
		/*
		 * https://regex101.com/
		 * ([A-Z]+)\(
		 * BUILTINS.put(ResourceLocation.withDefaultNamespace("\L$1\E"), Tiers.$1);\n
		 */
		BUILTINS.put(ResourceLocation.withDefaultNamespace("wood"), Tiers.WOOD);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("stone"), Tiers.STONE);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("iron"), Tiers.IRON);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("diamond"), Tiers.DIAMOND);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("gold"), Tiers.GOLD);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("netherite"), Tiers.NETHERITE);
	}

	public static void refreshWithValues(Map<ResourceLocation, ? extends Tier> values) {
		ALL.clear();
		ALL.putAll(BUILTINS);
		ALL.putAll(values);
	}
}
