package snownee.kiwi.recipe;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class AlternativesIngredientBuilder {
	private final HolderGetter<Item> lookup;
	List<Ingredient> ingredients = Lists.newArrayList();

	public AlternativesIngredientBuilder(HolderGetter<Item> lookup) {
		this.lookup = lookup;
	}

	public static AlternativesIngredientBuilder of(HolderGetter<Item> lookup) {
		return new AlternativesIngredientBuilder(lookup);
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
		ingredients.add(RecipeUtil.tagIngredient(lookup, tag));
		return this;
	}

	public AlternativesIngredientBuilder add(CustomIngredient ingredient) {
		add(ingredient.toVanilla());
		return this;
	}

	public AlternativesIngredientBuilder add(String tagOrItem) {
		if (tagOrItem.startsWith("#")) {
			add(TagKey.create(Registries.ITEM, Identifier.parse(tagOrItem.substring(1))));
		} else {
			Item item = lookup.getOrThrow(ResourceKey.create(Registries.ITEM, Identifier.parse(tagOrItem))).value();
			Preconditions.checkState(item != Items.AIR);
			add(item);
		}
		return this;
	}

	public AlternativesIngredient build() {
		List<JsonElement> list = Lists.newArrayListWithExpectedSize(ingredients.size());
		for (Ingredient ingredient : ingredients) {
			list.add(Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, ingredient).result().orElseThrow());
		}
		return new AlternativesIngredient(list);
	}
}
