package snownee.kiwi.recipe;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;

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

	public AlternativesIngredientBuilder add(ICustomIngredient ingredient) {
		add(ingredient.toVanilla());
		return this;
	}

	public AlternativesIngredientBuilder add(String tagOrItem) {
		if (tagOrItem.startsWith("#")) {
			add(TagKey.create(Registries.ITEM, ResourceLocation.parse(tagOrItem.substring(1))));
		} else {
			Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(tagOrItem));
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