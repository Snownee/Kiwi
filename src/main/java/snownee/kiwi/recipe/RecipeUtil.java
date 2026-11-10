package snownee.kiwi.recipe;

import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
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
			public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> registryKey) {
				return output.lookup(registryKey);
			}

			@Override
			public <S> Stream<Holder.Reference<S>> listContextElements(ResourceKey<? extends Registry<? extends S>> registryKey) {
				return output.listContextElements(registryKey);
			}
		};
	}
}
