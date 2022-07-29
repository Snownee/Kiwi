package snownee.kiwi.recipe;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.common.crafting.CompoundIngredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.common.crafting.conditions.ICondition.IContext;
import snownee.kiwi.mixin.RecipeManagerAccess;
import snownee.kiwi.util.Util;

public enum AlternativesIngredientSerializer implements IIngredientSerializer<Ingredient> {
	INSTANCE;

	@Override
	public Ingredient parse(JsonObject json) {
		RecipeManager recipeManager = Util.getRecipeManager();
		if (recipeManager == null) {
			throw new JsonSyntaxException("Unable to get recipe manager");
		}
		IContext ctx = ((RecipeManagerAccess) recipeManager).getContext();

		JsonArray list = GsonHelper.getAsJsonArray(json, "list");
		List<Ingredient> ingredients = Lists.newArrayList();
		for (JsonElement e : list) {
			if (e.isJsonArray()) {
				JsonArray a = e.getAsJsonArray();
				if (a.size() == 0) {
					return Ingredient.EMPTY;
				}
				for (JsonElement e2 : a) {
					try {
						ingredients.add(getIngredient(e2, ctx));
					} catch (Exception ignore) {
					}
				}
			} else {
				try {
					ingredients.add(getIngredient(e, ctx));
				} catch (Exception ignore) {
				}
			}
			if (!ingredients.isEmpty()) {
				if (ingredients.size() == 1) {
					return ingredients.get(0);
				} else {
					try {
						return CompoundIngredient.of(ingredients.toArray(Ingredient[]::new));
					} catch (Exception e1) {
						break;
					}
				}
			}
		}
		throw new JsonSyntaxException("Mismatched: " + json);
	}

	public static Ingredient getIngredient(JsonElement e, IContext ctx) {
		if (e.isJsonObject()) {
			JsonObject o = e.getAsJsonObject();
			if (o.size() == 1 && o.has("tag")) {
				ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(o, "tag"));
				TagKey<Item> tagkey = TagKey.create(Registry.ITEM_REGISTRY, resourcelocation);
				if (ctx.getTag(tagkey).isEmpty()) {
					throw new JsonSyntaxException("hasNoMatchingItems");
				}
			}
		}
		Ingredient ingredient = CraftingHelper.getIngredient(e);
		if (ingredient.isEmpty()) {
			throw new JsonSyntaxException("hasNoMatchingItems");
		}
		return ingredient;
	}

	@Override
	public Ingredient parse(FriendlyByteBuf buffer) {
		throw new IllegalStateException();
	}

	@Override
	public void write(FriendlyByteBuf buffer, Ingredient ingredient) {
		throw new IllegalStateException();
	}

}
