package snownee.kiwi.recipe.crafting;

import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.CraftingRecipeBuilder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import snownee.kiwi.data.DataModule;

public class KiwiShapelessRecipeBuilder
		extends CraftingRecipeBuilder
		implements RecipeBuilder {
	private final RecipeCategory category;
	private final Item result;
	private final int count;
	private final List<Ingredient> ingredients = Lists.newArrayList();
	private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();
	@Nullable
	private String group;
	private boolean noContainers;

	public KiwiShapelessRecipeBuilder(RecipeCategory recipeCategory, ItemLike itemLike, int i) {
		this.category = recipeCategory;
		this.result = itemLike.asItem();
		this.count = i;
	}

	public static KiwiShapelessRecipeBuilder shapeless(RecipeCategory recipeCategory, ItemLike itemLike) {
		return new KiwiShapelessRecipeBuilder(recipeCategory, itemLike, 1);
	}

	public static KiwiShapelessRecipeBuilder shapeless(RecipeCategory recipeCategory, ItemLike itemLike, int i) {
		return new KiwiShapelessRecipeBuilder(recipeCategory, itemLike, i);
	}

	public KiwiShapelessRecipeBuilder requires(TagKey<Item> tagKey) {
		return this.requires(Ingredient.of(tagKey));
	}

	public KiwiShapelessRecipeBuilder requires(ItemLike itemLike) {
		return this.requires(itemLike, 1);
	}

	public KiwiShapelessRecipeBuilder requires(ItemLike itemLike, int i) {
		for (int j = 0; j < i; ++j) {
			this.requires(Ingredient.of(itemLike));
		}
		return this;
	}

	public KiwiShapelessRecipeBuilder requires(Ingredient ingredient) {
		return this.requires(ingredient, 1);
	}

	public KiwiShapelessRecipeBuilder requires(Ingredient ingredient, int i) {
		for (int j = 0; j < i; ++j) {
			this.ingredients.add(ingredient);
		}
		return this;
	}

	@Override
	public KiwiShapelessRecipeBuilder unlockedBy(String string, CriterionTriggerInstance criterionTriggerInstance) {
		this.advancement.addCriterion(string, criterionTriggerInstance);
		return this;
	}

	@Override
	public KiwiShapelessRecipeBuilder group(@Nullable String string) {
		this.group = string;
		return this;
	}

	public KiwiShapelessRecipeBuilder noContainers() {
		this.noContainers = true;
		return this;
	}

	@Override
	public Item getResult() {
		return this.result;
	}

	@Override
	public void save(Consumer<FinishedRecipe> consumer, ResourceLocation resourceLocation) {
		this.ensureValid(resourceLocation);
		this.advancement.parent(ROOT_RECIPE_ADVANCEMENT).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(resourceLocation)).rewards(AdvancementRewards.Builder.recipe(resourceLocation)).requirements(RequirementsStrategy.OR);
		consumer.accept(new Result(resourceLocation, this.result, this.count, this.group == null ? "" : this.group, KiwiShapelessRecipeBuilder.determineBookCategory(this.category), this.ingredients, this.advancement, resourceLocation.withPrefix("recipes/" + this.category.getFolderName() + "/"), noContainers));
	}

	private void ensureValid(ResourceLocation resourceLocation) {
		if (this.advancement.getCriteria().isEmpty()) {
			throw new IllegalStateException("No way of obtaining recipe " + resourceLocation);
		}
	}

	public static class Result
			extends CraftingRecipeBuilder.CraftingResult {
		private final ResourceLocation id;
		private final Item result;
		private final int count;
		private final String group;
		private final List<Ingredient> ingredients;
		private final Advancement.Builder advancement;
		private final ResourceLocation advancementId;
		private final boolean noContainers;

		public Result(ResourceLocation resourceLocation, Item item, int i, String string, CraftingBookCategory craftingBookCategory, List<Ingredient> list, Advancement.Builder builder, ResourceLocation resourceLocation2, boolean noContainers) {
			super(craftingBookCategory);
			this.id = resourceLocation;
			this.result = item;
			this.count = i;
			this.group = string;
			this.ingredients = list;
			this.advancement = builder;
			this.advancementId = resourceLocation2;
			this.noContainers = noContainers;
		}

		@Override
		public void serializeRecipeData(JsonObject jsonObject) {
			super.serializeRecipeData(jsonObject);
			if (!this.group.isEmpty()) {
				jsonObject.addProperty("group", this.group);
			}
			JsonArray jsonArray = new JsonArray();
			for (Ingredient ingredient : this.ingredients) {
				jsonArray.add(ingredient.toJson());
			}
			jsonObject.add("ingredients", jsonArray);
			JsonObject jsonObject2 = new JsonObject();
			jsonObject2.addProperty("item", BuiltInRegistries.ITEM.getKey(this.result).toString());
			if (this.count > 1) {
				jsonObject2.addProperty("count", this.count);
			}
			jsonObject.add("result", jsonObject2);
			if (noContainers) {
				jsonObject.addProperty("no_containers", true);
			}
		}

		@Override
		public RecipeSerializer<?> getType() {
			return DataModule.SHAPELESS.get();
		}

		@Override
		public ResourceLocation getId() {
			return this.id;
		}

		@Override
		@Nullable
		public JsonObject serializeAdvancement() {
			return this.advancement.serializeToJson();
		}

		@Override
		@Nullable
		public ResourceLocation getAdvancementId() {
			return this.advancementId;
		}
	}
}

