package snownee.kiwi.recipe;

import net.minecraft.core.HolderGetter;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

public final class RecipeUtil {
	private RecipeUtil() {
		// Utility class
	}

	public static Ingredient tagIngredient(HolderGetter<Item> lookup, TagKey<Item> tag) {
		return Ingredient.of(lookup.getOrThrow(tag));
	}
}
