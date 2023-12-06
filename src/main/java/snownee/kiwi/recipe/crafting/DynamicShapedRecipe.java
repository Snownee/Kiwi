package snownee.kiwi.recipe.crafting;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;

public abstract class DynamicShapedRecipe extends CustomRecipe {
	protected ShapedRecipePattern pattern;
	protected String rawPattern;
	protected boolean differentInputs;
	protected boolean showNotification;
	protected ItemStack result;
	protected String group;

	public DynamicShapedRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result, boolean showNotification, boolean differentInputs) {
		super(category);
		this.group = group;
		this.pattern = pattern;
		this.rawPattern = String.join("", pattern.data().orElseThrow().pattern());
		this.result = result;
		this.showNotification = showNotification;
		this.differentInputs = differentInputs;
	}

	public DynamicShapedRecipe(CraftingBookCategory category) {
		super(category);
	}

	@Override
	public boolean matches(CraftingContainer inv, Level worldIn) {
		return search(inv) != null;
	}

	@Nullable
	public int[] search(CraftingContainer inv) {
		for (int x = 0; x <= inv.getWidth() - getWidth(); ++x) {
			for (int y = 0; y <= inv.getHeight() - getHeight(); ++y) {
				if (checkMatch(inv, x, y) && checkEmpty(inv, x, y)) {
					return new int[]{x, y};
				}
			}
		}
		return null;
	}

	public ItemStack item(char key, CraftingContainer inv, int[] matchPos) {
		int i = rawPattern.indexOf(key);
		if (i != -1) {
			int x = matchPos[0] + i % getWidth();
			int y = matchPos[1] + i / getWidth();
			return inv.getItem(x + y * inv.getWidth());
		}
		return ItemStack.EMPTY;
	}

	public List<ItemStack> items(char key, CraftingContainer inv, int[] matchPos) {
		List<ItemStack> items = Lists.newArrayList();
		for (int i = 0; i < rawPattern.length(); i++) {
			if (key == rawPattern.charAt(i)) {
				int x = matchPos[0] + i % getWidth();
				int y = matchPos[1] + i / getWidth();
				items.add(inv.getItem(x + y * inv.getWidth()));
			}
		}
		return items;
	}

	@Override
	public abstract ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess);

	public int getWidth() {
		return pattern.width();
	}

	public int getHeight() {
		return pattern.height();
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width >= getWidth() && height >= getHeight();
	}

	@Override
	public String getGroup() {
		return group;
	}

	@Override
	public boolean showNotification() {
		return showNotification;
	}

	public ItemStack result() {
		return result;
	}

	public ShapedRecipePattern pattern() {
		return pattern;
	}

	public boolean allowDifferentInputs() {
		return differentInputs;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return pattern.ingredients();
	}

	@Override
	public abstract RecipeSerializer<?> getSerializer();

	protected boolean checkMatch(CraftingContainer inv, int startX, int startY) {
		Char2ObjectArrayMap<ItemStack> ingredientsArrayMap = null;
		if (!differentInputs) {
			ingredientsArrayMap = new Char2ObjectArrayMap<>();
		}
		for (int y = startY; y < startY + getHeight(); ++y) {
			for (int x = startX; x < startX + getWidth(); ++x) {
				int rx = x - startX;
				int ry = y - startY;
				if (!matches(inv, x, y, rx, ry)) {
					return false;
				}
				if (!differentInputs) {
					int i = rx + ry * getWidth();
					char key = rawPattern.charAt(i);
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
		Ingredient ingredient = getIngredients().get(rx + ry * getWidth());
		return ingredient.test(inv.getItem(x + y * inv.getWidth()));
	}

	protected boolean checkEmpty(CraftingContainer inv, int startX, int startY) {
		for (int y = 0; y < inv.getHeight(); ++y) {
			int subY = y - startY;
			for (int x = 0; x < inv.getWidth(); ++x) {
				int subX = x - startX;
				if (subX >= 0 && subY >= 0 && subX < getWidth() && subY < getHeight()) {
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

	public static abstract class Serializer<T extends DynamicShapedRecipe> implements RecipeSerializer<T> {
		public static <T extends DynamicShapedRecipe> T fromNetwork(Function<CraftingBookCategory, T> constructor, FriendlyByteBuf buffer) {
			T recipe = constructor.apply(buffer.readEnum(CraftingBookCategory.class));
			recipe.group = buffer.readUtf(256);
			recipe.result = buffer.readItem();
			recipe.pattern = ShapedRecipePattern.fromNetwork(buffer);
			recipe.differentInputs = buffer.readBoolean();
			return recipe;
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, T recipe) {
			buffer.writeEnum(recipe.category());
			buffer.writeUtf(recipe.getGroup(), 256);
			buffer.writeItem(recipe.result);
			recipe.pattern.toNetwork(buffer);
			recipe.rawPattern = String.join("", recipe.pattern.data().orElseThrow().pattern());
			buffer.writeBoolean(recipe.differentInputs);
		}
	}
}
