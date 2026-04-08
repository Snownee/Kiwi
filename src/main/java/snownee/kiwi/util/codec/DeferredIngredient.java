package snownee.kiwi.util.codec;

import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import net.minecraft.world.item.crafting.Ingredient;

public class DeferredIngredient implements Supplier<Ingredient> {
	public static final Codec<DeferredIngredient> CODEC = Ingredient.CODEC.xmap(DeferredIngredient::new, DeferredIngredient::get);

	private final Ingredient ingredient;

	public DeferredIngredient(Ingredient ingredient) {
		this.ingredient = ingredient;
	}

	@Override
	public Ingredient get() {
		return ingredient;
	}
}
