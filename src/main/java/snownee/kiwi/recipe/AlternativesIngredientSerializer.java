/*
package snownee.kiwi.recipe;

import java.lang.reflect.Constructor;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.common.crafting.CompoundIngredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import snownee.kiwi.Kiwi;

public enum AlternativesIngredientSerializer implements IIngredientSerializer<Ingredient> {
	INSTANCE;

	private static Constructor<CompoundIngredient> constructor;

	static {
		try {
			constructor = CompoundIngredient.class.getDeclaredConstructor(List.class);
			constructor.setAccessible(true);
		} catch (Exception e) {
			Kiwi.logger.catching(e);
		}
	}

	@Override
	public Ingredient parse(JsonObject json) {
		JsonArray list = GsonHelper.getAsJsonObject(json, "list");
		List<Ingredient> ingredients = Lists.newArrayList();
		for (JsonElement e : list) {
			if (e.isJsonArray()) {
				JsonArray a = e.getAsJsonArray();
				if (a.size() == 0) {
					return Ingredient.EMPTY;
				}
				for (JsonElement e2 : a) {
					try {
						ingredients.add(getIngredient(e2));
					} catch (Exception ignore) {
					}
				}
			} else {
				try {
					ingredients.add(getIngredient(e));
				} catch (Exception ignore) {
				}
			}
			if (!ingredients.isEmpty()) {
				if (ingredients.size() == 1) {
					return ingredients.get(0);
				} else {
					try {
						return constructor.newInstance(ingredients);
					} catch (Exception e1) {
						break;
					}
				}
			}
		}
		throw new JsonSyntaxException("Mismatched");
	}

	public static Ingredient getIngredient(JsonElement e) {
		Ingredient ingredient = CraftingHelper.getIngredient(e);
		if (ingredient.isEmpty()) {
			throw new JsonSyntaxException("hasNoMatchingItems");
		}
		if (!ForgeConfig.SERVER.treatEmptyTagsAsAir.get()) {
			ItemStack[] stacks = ingredient.getItems();
			if (stacks.length == 1 && stacks[0].getItem() == Items.BARRIER) {
				throw new JsonSyntaxException("hasNoMatchingItems");
			}
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
*/