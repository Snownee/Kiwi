package snownee.kiwi.customization.block.tier;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ToolMaterial;
import snownee.kiwi.util.codec.CustomizationCodecs;

public final class KiwiTiers {
	public static final BiMap<Identifier, ToolMaterial> ALL = HashBiMap.create();
	public static final Map<Identifier, ToolMaterial> BUILTINS = new HashMap<>();
	public static final Codec<ToolMaterial> CODEC = CustomizationCodecs.simpleByNameCodec(ALL);

	static {
		BUILTINS.put(Identifier.withDefaultNamespace("wood"), ToolMaterial.WOOD);
		BUILTINS.put(Identifier.withDefaultNamespace("stone"), ToolMaterial.STONE);
		BUILTINS.put(Identifier.withDefaultNamespace("iron"), ToolMaterial.IRON);
		BUILTINS.put(Identifier.withDefaultNamespace("diamond"), ToolMaterial.DIAMOND);
		BUILTINS.put(Identifier.withDefaultNamespace("gold"), ToolMaterial.GOLD);
		BUILTINS.put(Identifier.withDefaultNamespace("netherite"), ToolMaterial.NETHERITE);
	}

	public static void refreshWithValues(Map<Identifier, ? extends ToolMaterial> values) {
		ALL.clear();
		ALL.putAll(BUILTINS);
		ALL.putAll(values);
	}

	private KiwiTiers() {
	}
}
