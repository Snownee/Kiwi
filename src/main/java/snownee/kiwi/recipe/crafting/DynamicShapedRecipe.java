package snownee.kiwi.recipe.crafting;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipeCodecs;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import snownee.kiwi.recipe.KiwiRecipeSerializer;
import snownee.kiwi.util.Util;

public abstract class DynamicShapedRecipe extends CustomRecipe {
	public String pattern;
	public boolean differentInputs;
	public boolean showNotification;
	public ItemStack recipeOutput;
	public int width;
	public int height;
	public NonNullList<Ingredient> recipeItems;
	public String group;

	public DynamicShapedRecipe(CraftingBookCategory category) {
		super(category);
	}

	@Override
	public boolean matches(CraftingContainer inv, Level worldIn) {
		return search(inv) != null;
	}

	@Nullable
	public int[] search(CraftingContainer inv) {
		for (int x = 0; x <= inv.getWidth() - getRecipeWidth(); ++x) {
			for (int y = 0; y <= inv.getHeight() - getRecipeHeight(); ++y) {
				if (checkMatch(inv, x, y) && checkEmpty(inv, x, y)) {
					return new int[]{x, y};
				}
			}
		}
		return null;
	}

	public ItemStack item(char key, CraftingContainer inv, int[] matchPos) {
		int i = pattern.indexOf(key);
		if (i != -1) {
			int x = matchPos[0] + i % width;
			int y = matchPos[1] + i / width;
			return inv.getItem(x + y * inv.getWidth());
		}
		return ItemStack.EMPTY;
	}

	public List<ItemStack> items(char key, CraftingContainer inv, int[] matchPos) {
		List<ItemStack> items = Lists.newArrayList();
		for (int i = 0; i < pattern.length(); i++) {
			if (key == pattern.charAt(i)) {
				int x = matchPos[0] + i % width;
				int y = matchPos[1] + i / width;
				items.add(inv.getItem(x + y * inv.getWidth()));
			}
		}
		return items;
	}

	@Override
	public abstract ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess);

	//	@Override
	public int getRecipeWidth() {
		return width;
	}

	//	@Override
	public int getRecipeHeight() {
		return height;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width >= getRecipeWidth() && height >= getRecipeHeight();
	}

	@Override
	public String getGroup() {
		return group;
	}

	@Override
	public boolean showNotification() {
		return showNotification;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return recipeItems;
	}

	@Override
	public abstract RecipeSerializer<?> getSerializer();

	protected boolean checkMatch(CraftingContainer inv, int startX, int startY) {
		Char2ObjectArrayMap<ItemStack> ingredientsArrayMap = null;
		if (!differentInputs) {
			ingredientsArrayMap = new Char2ObjectArrayMap<>();
		}
		for (int y = startY; y < startY + getRecipeHeight(); ++y) {
			for (int x = startX; x < startX + getRecipeWidth(); ++x) {
				int rx = x - startX;
				int ry = y - startY;
				if (!matches(inv, x, y, rx, ry)) {
					return false;
				}
				if (!differentInputs) {
					int i = rx + ry * getRecipeWidth();
					char key = pattern.charAt(i);
					if (key != ' ') {
						ItemStack stack0 = inv.getItem(x + y * inv.getWidth());
						ItemStack stack1 = ingredientsArrayMap.get(key);
						if (stack1 == null) {
							ingredientsArrayMap.put(key, stack0);
						} else if (!ItemStack.isSameItemSameTags(stack1, stack0)) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	public boolean matches(CraftingContainer inv, int x, int y, int rx, int ry) {
		Ingredient ingredient = recipeItems.get(rx + ry * getRecipeWidth());
		return ingredient.test(inv.getItem(x + y * inv.getWidth()));
	}

	protected boolean checkEmpty(CraftingContainer inv, int startX, int startY) {
		for (int y = 0; y < inv.getHeight(); ++y) {
			int subY = y - startY;
			for (int x = 0; x < inv.getWidth(); ++x) {
				int subX = x - startX;
				if (subX >= 0 && subY >= 0 && subX < getRecipeWidth() && subY < getRecipeHeight()) {
					continue;
				}

				if (!getEmptyPredicate().test(inv.getItem(x + y * inv.getWidth()))) {
					return false;
				}
			}
		}
		return true;
	}

	protected Predicate<ItemStack> getEmptyPredicate() {
		return Ingredient.EMPTY;
	}

	public static abstract class Serializer<T extends DynamicShapedRecipe> extends KiwiRecipeSerializer<T> {
		public static void fromJson(DynamicShapedRecipe recipe, JsonObject json) {
			recipe.group = GsonHelper.getAsString(json, "group", "");
			Map<String, Ingredient> ingredientMap = Util.parseJson(ExtraCodecs.strictUnboundedMap(
					ShapedRecipe.Serializer.SINGLE_CHARACTER_STRING_CODEC, Ingredient.CODEC_NONEMPTY
			), GsonHelper.getAsJsonObject(json, "key"));
			String[] pattern = ShapedRecipe.shrink(Util.parseJson(ShapedRecipe.Serializer.PATTERN_CODEC, GsonHelper.getAsJsonArray(json, "pattern")));
			recipe.pattern = String.join("", pattern);
			recipe.width = pattern[0].length();
			recipe.height = pattern.length;
			recipe.recipeItems = NonNullList.withSize(recipe.width * recipe.height, Ingredient.EMPTY);
			Set<String> set = Sets.newHashSet(ingredientMap.keySet());

			for (int k = 0; k < pattern.length; ++k) {
				String string = pattern[k];

				for (int l = 0; l < string.length(); ++l) {
					String string2 = string.substring(l, l + 1);
					Ingredient ingredient = string2.equals(" ") ? Ingredient.EMPTY : ingredientMap.get(string2);
					if (ingredient == null) {
						throw new JsonParseException("Pattern references symbol '" + string2 + "' but it's not defined in the key");
					}

					set.remove(string2);
					recipe.recipeItems.set(l + recipe.width * k, ingredient);
				}
			}

			recipe.recipeOutput = Util.parseJson(CraftingRecipeCodecs.ITEMSTACK_OBJECT_CODEC, GsonHelper.getAsJsonObject(json, "result"));
			recipe.differentInputs = GsonHelper.getAsBoolean(json, "different_inputs", false);
			recipe.showNotification = GsonHelper.getAsBoolean(json, "show_notification", true);
		}

		public static void fromNetwork(DynamicShapedRecipe recipe, FriendlyByteBuf buffer) {
			recipe.width = buffer.readVarInt();
			recipe.height = buffer.readVarInt();
			recipe.group = buffer.readUtf(256);
			int size = recipe.width * recipe.height;
			recipe.recipeItems = NonNullList.withSize(size, Ingredient.EMPTY);
			for (int k = 0; k < size; ++k) {
				recipe.recipeItems.set(k, Ingredient.fromNetwork(buffer));
			}
			recipe.recipeOutput = buffer.readItem();
			recipe.pattern = buffer.readUtf(size);
			recipe.differentInputs = buffer.readBoolean();
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, T recipe) {
			buffer.writeVarInt(recipe.getRecipeWidth());
			buffer.writeVarInt(recipe.getRecipeHeight());
			buffer.writeUtf(recipe.getGroup(), 256);
			for (Ingredient ingredient : recipe.getIngredients()) {
				ingredient.toNetwork(buffer);
			}
			buffer.writeItem(recipe.recipeOutput);
			buffer.writeUtf(recipe.pattern, recipe.getIngredients().size());
			buffer.writeBoolean(recipe.differentInputs);
		}
	}
}
