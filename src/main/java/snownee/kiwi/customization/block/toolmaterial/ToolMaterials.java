package snownee.kiwi.customization.block.toolmaterial;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
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
	public static final BiMap<ResourceLocation, ToolMaterial> ALL = HashBiMap.create();
	public static final Map<ResourceLocation, ToolMaterial> BUILTINS = new HashMap<>();
	public static final Codec<ToolMaterial> CODEC = CustomizationCodecs.simpleByNameCodec(ALL);

	static {
		BUILTINS.put(ResourceLocation.withDefaultNamespace("wood"), ToolMaterial.WOOD);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("stone"), ToolMaterial.STONE);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("iron"), ToolMaterial.IRON);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("diamond"), ToolMaterial.DIAMOND);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("gold"), ToolMaterial.GOLD);
		BUILTINS.put(ResourceLocation.withDefaultNamespace("netherite"), ToolMaterial.NETHERITE);
	}

	public static void refreshWithValues(Map<ResourceLocation, ? extends ToolMaterial> values) {
		ALL.clear();
		ALL.putAll(BUILTINS);
		ALL.putAll(values);
	}
}
