package snownee.kiwi.customization.block.tier;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import snownee.kiwi.util.codec.CustomizationCodecs;

public final class KiwiTiers {
	public static final BiMap<Identifier, Tier> ALL = HashBiMap.create();
	public static final Map<Identifier, Tier> BUILTINS = new HashMap<>();
	public static final Codec<Tier> CODEC = CustomizationCodecs.simpleByNameCodec(ALL);

	static {
		/*
		 * https://regex101.com/
		 * ([A-Z]+)\(
		 * BUILTINS.put(Identifier.withDefaultNamespace("\L$1\E"), Tiers.$1);\n
		 */
		BUILTINS.put(Identifier.withDefaultNamespace("wood"), Tiers.WOOD);
		BUILTINS.put(Identifier.withDefaultNamespace("stone"), Tiers.STONE);
		BUILTINS.put(Identifier.withDefaultNamespace("iron"), Tiers.IRON);
		BUILTINS.put(Identifier.withDefaultNamespace("diamond"), Tiers.DIAMOND);
		BUILTINS.put(Identifier.withDefaultNamespace("gold"), Tiers.GOLD);
		BUILTINS.put(Identifier.withDefaultNamespace("netherite"), Tiers.NETHERITE);
	}

	public static void refreshWithValues(Map<Identifier, ? extends Tier> values) {
		ALL.clear();
		ALL.putAll(BUILTINS);
		ALL.putAll(values);
	}
}
