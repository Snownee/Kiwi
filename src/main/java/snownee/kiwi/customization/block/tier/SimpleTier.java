package snownee.kiwi.customization.block.tier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ToolMaterial;

public final class SimpleTier {
	public static final MapCodec<ToolMaterial> DIRECT_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			TagKey.codec(Registries.BLOCK).fieldOf("incorrect_blocks_for_drops").forGetter(ToolMaterial::incorrectBlocksForDrops),
			Codec.INT.fieldOf("uses").forGetter(ToolMaterial::durability),
			Codec.FLOAT.fieldOf("speed").forGetter(ToolMaterial::speed),
			Codec.FLOAT.fieldOf("attack_damage_bonus").forGetter(ToolMaterial::attackDamageBonus),
			Codec.INT.fieldOf("enchantment_value").forGetter(ToolMaterial::enchantmentValue),
			TagKey.codec(Registries.ITEM).fieldOf("repair_items").forGetter(ToolMaterial::repairItems)
	).apply(instance, ToolMaterial::new));

	private SimpleTier() {
	}
}
