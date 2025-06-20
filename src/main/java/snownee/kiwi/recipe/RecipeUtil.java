package snownee.kiwi.recipe;

import java.util.stream.Stream;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

public final class RecipeUtil {
	private static final Ingredient EMPTY_INGREDIENT = Ingredient.of(Stream.empty());

	private RecipeUtil() {
		// Utility class
	}

	public static Ingredient tagIngredient(TagKey<Item> tag) {
		return Ingredient.of(BuiltInRegistries.ITEM.get(tag).orElseThrow());
	}

	public static Ingredient emptyIngredient() {
		return EMPTY_INGREDIENT;
	}
}
