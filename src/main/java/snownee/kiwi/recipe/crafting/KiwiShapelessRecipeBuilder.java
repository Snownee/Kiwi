package snownee.kiwi.recipe.crafting;

import java.util.Objects;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import snownee.kiwi.util.NotNullByDefault;

@NotNullByDefault
public class KiwiShapelessRecipeBuilder extends ShapelessRecipeBuilder {
	private boolean noContainers;

	public KiwiShapelessRecipeBuilder(RecipeCategory category, ItemLike result, int count) {
		super(category, result, count);
	}

	public static KiwiShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike result) {
		return new KiwiShapelessRecipeBuilder(category, result, 1);
	}

	public static KiwiShapelessRecipeBuilder shapeless(RecipeCategory category, ItemLike result, int count) {
		return new KiwiShapelessRecipeBuilder(category, result, count);
	}

	public KiwiShapelessRecipeBuilder noContainers() {
		this.noContainers = true;
		return this;
	}

	@Override
	public void save(RecipeOutput recipeOutput, ResourceLocation resourceLocation) {
		ensureValid(resourceLocation);
		Advancement.Builder builder = recipeOutput.advancement().addCriterion(
				"has_the_recipe",
				RecipeUnlockedTrigger.unlocked(resourceLocation)).rewards(AdvancementRewards.Builder.recipe(resourceLocation)).requirements(
				AdvancementRequirements.Strategy.OR);
		Objects.requireNonNull(builder);
		criteria.forEach(builder::addCriterion);
		KiwiShapelessRecipe shapelessRecipe = new KiwiShapelessRecipe(
				Objects.requireNonNullElse(group, ""),
				RecipeBuilder.determineBookCategory(category),
				new ItemStack(result, count),
				ingredients,
				noContainers);
		recipeOutput.accept(
				resourceLocation,
				shapelessRecipe,
				builder.build(resourceLocation.withPrefix("recipes/" + category.getFolderName() + "/")));
	}
}

