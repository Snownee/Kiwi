package snownee.kiwi.recipe.crafting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.advancements.Criterion;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeUnlockAdvancementBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

public class KiwiShapelessRecipeBuilder implements RecipeBuilder {
	private final RecipeCategory category;
	private final ItemStackTemplate result;
	private final List<Ingredient> ingredients = new ArrayList<>();
	private final RecipeUnlockAdvancementBuilder advancementBuilder = new RecipeUnlockAdvancementBuilder();
	private @Nullable String group;
	private boolean noContainers;

	public KiwiShapelessRecipeBuilder(RecipeCategory category, ItemLike result, int count) {
		this.category = category;
		this.result = new ItemStackTemplate(result.asItem(), count);
	}

	public static KiwiShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike result) {
		return new KiwiShapelessRecipeBuilder(category, result, 1);
	}

	public static KiwiShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike result, int count) {
		return new KiwiShapelessRecipeBuilder(category, result, count);
	}

	public KiwiShapelessRecipeBuilder requires(ItemLike item) {
		return requires(item, 1);
	}

	public KiwiShapelessRecipeBuilder requires(ItemLike item, int count) {
		for (int i = 0; i < count; i++) {
			requires(Ingredient.of(item));
		}
		return this;
	}

	public KiwiShapelessRecipeBuilder requires(Ingredient ingredient) {
		return requires(ingredient, 1);
	}

	public KiwiShapelessRecipeBuilder requires(Ingredient ingredient, int count) {
		for (int i = 0; i < count; i++) {
			ingredients.add(ingredient);
		}
		return this;
	}

	public KiwiShapelessRecipeBuilder noContainers() {
		this.noContainers = true;
		return this;
	}

	@Override
	public KiwiShapelessRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
		advancementBuilder.unlockedBy(name, criterion);
		return this;
	}

	@Override
	public KiwiShapelessRecipeBuilder group(@Nullable String group) {
		this.group = group;
		return this;
	}

	@Override
	public ResourceKey<Recipe<?>> defaultId() {
		return RecipeBuilder.getDefaultRecipeId(result);
	}

	@Override
	public void save(RecipeOutput recipeOutput, ResourceKey<Recipe<?>> id) {
		NonNullList<Ingredient> nonNullIngredients = NonNullList.create();
		nonNullIngredients.addAll(ingredients);
		KiwiShapelessRecipe shapelessRecipe = new KiwiShapelessRecipe(
				Objects.requireNonNullElse(group, ""),
				RecipeBuilder.determineCraftingBookCategory(category),
				result.create(),
				nonNullIngredients,
				noContainers);
		recipeOutput.accept(id, shapelessRecipe, advancementBuilder.build(recipeOutput, id, category));
	}
}

