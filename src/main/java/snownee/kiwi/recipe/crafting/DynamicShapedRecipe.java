package snownee.kiwi.recipe.crafting;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMaps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
import snownee.kiwi.util.NotNullByDefault;

@NotNullByDefault
public abstract class DynamicShapedRecipe extends CustomRecipe {
	protected ShapedRecipePattern pattern;
	protected String rawPattern;
	protected boolean differentInputs;
	protected boolean showNotification;
	protected ItemStack result;
	protected String group;

	public DynamicShapedRecipe(
			String group,
			CraftingBookCategory category,
			ShapedRecipePattern pattern,
			ItemStack result,
			boolean showNotification,
			boolean differentInputs) {
		super(category);
		this.group = group;
		this.pattern = pattern;
		this.rawPattern = String.join("", pattern.data.orElseThrow().pattern());
		this.result = result;
		this.showNotification = showNotification;
		this.differentInputs = differentInputs;
	}

	public DynamicShapedRecipe(CraftingBookCategory category) {
		super(category);
	}

	@Override
	public boolean matches(CraftingInput input, Level worldIn) {
		return search(input) != null;
	}

	public int @Nullable [] search(CraftingInput input) {
		for (int x = 0; x <= input.width() - getWidth(); ++x) {
			for (int y = 0; y <= input.height() - getHeight(); ++y) {
				if (checkMatch(input, x, y) && checkEmpty(input, x, y)) {
					return new int[]{x, y};
				}
			}
		}
		return null;
	}

	public ItemStack item(char key, CraftingInput inv, int[] matchPos) {
		int i = rawPattern.indexOf(key);
		if (i != -1) {
			int x = matchPos[0] + i % getWidth();
			int y = matchPos[1] + i / getWidth();
			return inv.getItem(x + y * inv.width());
		}
		return ItemStack.EMPTY;
	}

	public List<ItemStack> items(char key, CraftingInput inv, int[] matchPos) {
		List<ItemStack> items = Lists.newArrayList();
		for (int i = 0; i < rawPattern.length(); i++) {
			if (key == rawPattern.charAt(i)) {
				int x = matchPos[0] + i % getWidth();
				int y = matchPos[1] + i / getWidth();
				items.add(inv.getItem(x + y * inv.width()));
			}
		}
		return items;
	}

	@Override
	public abstract ItemStack assemble(CraftingInput inv, HolderLookup.Provider registryAccess);

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

	protected boolean checkMatch(CraftingInput input, int startX, int startY) {
		Char2ObjectMap<ItemStack> ingredientsArrayMap = Char2ObjectMaps.emptyMap();
		if (!differentInputs) {
			ingredientsArrayMap = new Char2ObjectArrayMap<>();
		}
		for (int y = startY; y < startY + getHeight(); ++y) {
			for (int x = startX; x < startX + getWidth(); ++x) {
				int rx = x - startX;
				int ry = y - startY;
				if (!matches(input, x, y, rx, ry)) {
					return false;
				}
				if (!differentInputs) {
					int i = rx + ry * getWidth();
					char key = rawPattern.charAt(i);
					if (key != ' ') {
						ItemStack stack0 = input.getItem(x + y * input.width());
						ItemStack stack1 = ingredientsArrayMap.get(key);
						if (stack1 == null) {
							ingredientsArrayMap.put(key, stack0);
						} else if (!ItemStack.isSameItemSameComponents(stack1, stack0)) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	public boolean matches(CraftingInput inv, int x, int y, int rx, int ry) {
		Ingredient ingredient = getIngredients().get(rx + ry * getWidth());
		return ingredient.test(inv.getItem(x + y * inv.width()));
	}

	protected boolean checkEmpty(CraftingInput inv, int startX, int startY) {
		for (int y = 0; y < inv.height(); ++y) {
			int subY = y - startY;
			for (int x = 0; x < inv.width(); ++x) {
				int subX = x - startX;
				if (subX >= 0 && subY >= 0 && subX < getWidth() && subY < getHeight()) {
					continue;
				}

				if (!getEmptyPredicate().test(inv.getItem(x + y * inv.width()))) {
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
		public static <T extends DynamicShapedRecipe> T fromNetwork(
				Function<CraftingBookCategory, T> constructor,
				RegistryFriendlyByteBuf buffer) {
			T recipe = constructor.apply(buffer.readEnum(CraftingBookCategory.class));
			recipe.group = buffer.readUtf(256);
			recipe.result = ItemStack.STREAM_CODEC.decode(buffer);
			recipe.pattern = ShapedRecipePattern.STREAM_CODEC.decode(buffer);
			recipe.differentInputs = buffer.readBoolean();
			return recipe;
		}

		public static <T extends DynamicShapedRecipe> void toNetwork(RegistryFriendlyByteBuf buffer, T recipe) {
			buffer.writeEnum(recipe.category());
			buffer.writeUtf(recipe.getGroup(), 256);
			ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
			ShapedRecipePattern.STREAM_CODEC.encode(buffer, recipe.pattern);
			recipe.rawPattern = String.join("", recipe.pattern.data.orElseThrow().pattern());
			buffer.writeBoolean(recipe.differentInputs);
		}
	}
}
