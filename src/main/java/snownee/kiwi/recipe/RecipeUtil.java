package snownee.kiwi.recipe;

import org.jspecify.annotations.Nullable;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

public final class RecipeUtil {
	private RecipeUtil() {
		// Utility class
	}

	public static Ingredient tagIngredient(HolderGetter<Item> lookup, TagKey<Item> tag) {
		return Ingredient.of(lookup.getOrThrow(tag));
	}

	public static RecipeOutput withNoRemainders(RecipeOutput output) {
		return new RecipeOutput() {
			@Override
			public void accept(ResourceKey<Recipe<?>> id, Recipe<?> recipe, @Nullable AdvancementHolder advancement) {
				if (recipe instanceof KiwiRecipe kiwiRecipe) {
					kiwiRecipe.kiwi$setNoRemainders(true);
				}
				output.accept(id, recipe, advancement);
			}

			@Override
			public Advancement.Builder advancement() {
				return output.advancement();
			}

			@Override
			public <S> HolderGetter<S> lookup(ResourceKey<? extends net.minecraft.core.Registry<? extends S>> key) {
				return output.lookup(key);
			}

			@Override
			public <S> java.util.stream.Stream<net.minecraft.core.Holder.Reference<S>> listContextElements(
					ResourceKey<? extends net.minecraft.core.Registry<? extends S>> key) {
				return output.listContextElements(key);
			}
		};
	}
}
