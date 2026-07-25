package snownee.kiwi.recipe;

import org.jspecify.annotations.Nullable;

import net.minecraft.resources.Identifier;

public interface CustomIngredientSerializer<T extends CustomIngredient> extends net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer<T> {
	static void register(CustomIngredientSerializer<?> serializer) {
		net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer.register(serializer);
	}

	@Nullable
	static CustomIngredientSerializer<?> get(Identifier identifier) {
		net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer<?> serializer = net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer.get(
				identifier);
		return serializer instanceof CustomIngredientSerializer ? (CustomIngredientSerializer<?>) serializer : null;
	}
}