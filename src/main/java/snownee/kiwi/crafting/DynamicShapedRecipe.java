package snownee.kiwi.crafting;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.IShapedRecipe;

public abstract class DynamicShapedRecipe implements ICraftingRecipe, IShapedRecipe<CraftingInventory> {
	private final int recipeWidth;
	private final int recipeHeight;
	private final NonNullList<Ingredient> recipeItems;
	private final ItemStack recipeOutput;
	private final ResourceLocation id;
	private final String group;

	public DynamicShapedRecipe(ResourceLocation idIn, String groupIn, int recipeWidthIn, int recipeHeightIn, NonNullList<Ingredient> ingredients, ItemStack recipeOutputIn) {
		id = idIn;
		group = groupIn;
		recipeWidth = recipeWidthIn;
		recipeHeight = recipeHeightIn;
		recipeItems = ingredients;
		recipeOutput = recipeOutputIn;
	}

	@Override
	public boolean matches(CraftingInventory inv, World worldIn) {
		return getMatchPos(inv) != null;
	}

	@Nullable
	public int[] getMatchPos(CraftingInventory inv) {
		for (int x = 0; x <= inv.getWidth() - getRecipeWidth(); ++x) {
			for (int y = 0; y <= inv.getHeight() - getRecipeHeight(); ++y) {
				if (checkMatch(inv, x, y) && checkEmpty(inv, x, y)) {
					return new int[] { x, y };
				}
			}
		}
		return null;
	}

	@Override
	public abstract ItemStack assemble(CraftingInventory inv);

	@Override
	public ItemStack getResultItem() {
		return recipeOutput;
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@Override
	public int getRecipeWidth() {
		return recipeWidth;
	}

	@Override
	public int getRecipeHeight() {
		return recipeHeight;
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
	public boolean isSpecial() {
		return true;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return recipeItems;
	}

	@Override
	public abstract IRecipeSerializer<?> getSerializer();

	protected boolean checkMatch(CraftingInventory inv, int startX, int startY) {
		for (int y = startY; y < startY + getRecipeHeight(); ++y) {
			for (int x = startX; x < startX + getRecipeWidth(); ++x) {
				if (!matches(inv, x, y, x - startX, y - startY)) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean matches(CraftingInventory inv, int x, int y, int ix, int iy) {
		Ingredient ingredient = recipeItems.get(ix + iy * getRecipeWidth());
		return ingredient.test(inv.getItem(x + y * inv.getWidth()));
	}

	protected boolean checkEmpty(CraftingInventory inv, int startX, int startY) {
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
}
