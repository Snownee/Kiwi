package snownee.kiwi.customization.item.toolmaterial;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ToolMaterial;
import snownee.kiwi.util.codec.CustomizationCodecs;

public final class ToolMaterials {
	public static final MapCodec<ToolMaterial> DIRECT_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			TagKey.codec(Registries.BLOCK).fieldOf("incorrect_blocks_for_drops").forGetter(ToolMaterial::incorrectBlocksForDrops),
			Codec.INT.fieldOf("uses").forGetter(ToolMaterial::durability),
			Codec.FLOAT.fieldOf("speed").forGetter(ToolMaterial::speed),
			Codec.FLOAT.fieldOf("attack_damage_bonus").forGetter(ToolMaterial::attackDamageBonus),
			Codec.INT.fieldOf("enchantment_value").forGetter(ToolMaterial::enchantmentValue),
			TagKey.codec(Registries.ITEM).fieldOf("repair_items").forGetter(ToolMaterial::repairItems)
	).apply(instance, ToolMaterial::new));
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
}
