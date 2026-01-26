package snownee.kiwi.recipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

public final class RecipeUtil {
	private RecipeUtil() {
		// Utility class
	}

	public static Ingredient tagIngredient(TagKey<Item> tag) {
		return Ingredient.of(BuiltInRegistries.ITEM.get(tag).orElseThrow());
	}
}
