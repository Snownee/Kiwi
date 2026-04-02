/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package snownee.kiwi.customization.block.tier;

import org.jspecify.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import snownee.kiwi.util.codec.DeferredIngredient;

public class SimpleTier implements Tier {
	public static final MapCodec<SimpleTier> DIRECT_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			TagKey.codec(Registries.BLOCK).fieldOf("incorrect_blocks_for_drops").forGetter(SimpleTier::getIncorrectBlocksForDrops),
			Codec.INT.fieldOf("uses").forGetter(SimpleTier::getUses),
			Codec.FLOAT.fieldOf("speed").forGetter(SimpleTier::getSpeed),
			Codec.FLOAT.fieldOf("attack_damage_bonus").forGetter(SimpleTier::getAttackDamageBonus),
			Codec.INT.fieldOf("enchantment_value").forGetter(SimpleTier::getEnchantmentValue),
			DeferredIngredient.CODEC.fieldOf("repair_ingredient").forGetter(it -> it.repairIngredient)
	).apply(instance, SimpleTier::new));

	private final TagKey<Block> incorrectBlocksForDrops;
	private final int uses;
	private final float speed;
	private final float attackDamageBonus;
	private final int enchantmentValue;
	private final DeferredIngredient repairIngredient;

	public SimpleTier(
			TagKey<Block> incorrectBlocksForDrops,
			int uses,
			float speed,
			float attackDamageBonus,
			int enchantmentValue,
			DeferredIngredient repairIngredient) {
		this.incorrectBlocksForDrops = incorrectBlocksForDrops;
		this.uses = uses;
		this.speed = speed;
		this.attackDamageBonus = attackDamageBonus;
		this.enchantmentValue = enchantmentValue;
		this.repairIngredient = repairIngredient;
	}

	@Override
	public int getUses() {
		return this.uses;
	}

	@Override
	public float getSpeed() {
		return this.speed;
	}

	@Override
	public float getAttackDamageBonus() {
		return this.attackDamageBonus;
	}

	@Override
	public TagKey<Block> getIncorrectBlocksForDrops() {
		return incorrectBlocksForDrops;
	}

	@Override
	public int getEnchantmentValue() {
		return this.enchantmentValue;
	}

	@Override
	public @NotNull Ingredient getRepairIngredient() {
		return this.repairIngredient.get();
	}

	@Override
	public String toString() {
		return "SimpleTier[" +
				"incorrectBlocksForDrops=" + incorrectBlocksForDrops + ", " +
				"uses=" + uses + ", " +
				"speed=" + speed + ", " +
				"attackDamageBonus=" + attackDamageBonus + ", " +
				"enchantmentValue=" + enchantmentValue + ", " +
				"repairIngredient=" + repairIngredient + ']';
	}
}
