package snownee.kiwi.recipe.crafting;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;

import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class DynamicShapedRecipe extends CustomRecipe implements IShapedRecipe<CraftingContainer> {
	private int width;
	private int height;
	private NonNullList<Ingredient> recipeItems;
	public String pattern;
	public boolean differentInputs;
	public ItemStack recipeOutput;
	private String group;

	public DynamicShapedRecipe(ResourceLocation idIn) {
		super(idIn);
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
					return new int[] { x, y };
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
	public abstract ItemStack assemble(CraftingContainer inv);

	@Override
	public int getRecipeWidth() {
		return width;
	}

	@Override
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
						} else if (!stack1.sameItemStackIgnoreDurability(stack0)) {
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

				if (!getEmpty().test(inv.getItem(x + y * inv.getWidth()))) {
					return false;
				}
			}
		}
		return true;
	}

	protected Predicate<ItemStack> getEmpty() {
		return Ingredient.EMPTY;
	}

	public static abstract class Serializer<T extends DynamicShapedRecipe> extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<T> {
		public static void fromJson(DynamicShapedRecipe recipe, JsonObject json) {
			recipe.group = GsonHelper.getAsString(json, "group", "");
			Map<String, Ingredient> ingredientMap = ShapedRecipe.keyFromJson(GsonHelper.getAsJsonObject(json, "key"));
			String[] pattern = ShapedRecipe.shrink(ShapedRecipe.patternFromJson(GsonHelper.getAsJsonArray(json, "pattern")));
			recipe.pattern = String.join("", pattern);
			recipe.width = pattern[0].length();
			recipe.height = pattern.length;
			recipe.recipeItems = ShapedRecipe.dissolvePattern(pattern, ingredientMap, recipe.width, recipe.height);
			recipe.recipeOutput = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
			recipe.differentInputs = GsonHelper.getAsBoolean(json, "differentInputs", false);
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
