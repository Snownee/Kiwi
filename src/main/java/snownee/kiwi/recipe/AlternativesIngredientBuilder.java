package snownee.kiwi.recipe;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class AlternativesIngredientBuilder {
	List<Ingredient> ingredients = Lists.newArrayList();

	public static AlternativesIngredientBuilder of() {
		return new AlternativesIngredientBuilder();
	}

	public AlternativesIngredientBuilder add(Ingredient ingredient) {
		ingredients.add(ingredient);
		return this;
	}

	public AlternativesIngredientBuilder add(ItemLike itemLike) {
		ingredients.add(Ingredient.of(itemLike));
		return this;
	}

	public AlternativesIngredientBuilder add(TagKey<Item> tag) {
		ingredients.add(Ingredient.of(tag));
		return this;
	}

	public AlternativesIngredientBuilder add(CustomIngredient ingredient) {
		add(ingredient.toVanilla());
		return this;
	}

	public AlternativesIngredientBuilder add(String tagOrItem) {
		if (tagOrItem.startsWith("#")) {
			add(TagKey.create(Registries.ITEM, new ResourceLocation(tagOrItem.substring(1))));
		} else {
			Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(tagOrItem));
			Preconditions.checkState(item != Items.AIR);
			add(item);
		}
		return this;
	}

	public AlternativesIngredient build() {
		JsonArray jsonArray = new JsonArray(ingredients.size());
		for (Ingredient ingredient : ingredients) {
			jsonArray.add(ingredient.toJson());
		}
		return new AlternativesIngredient(jsonArray);
	}
}
