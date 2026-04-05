package snownee.kiwi.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;

public abstract class SimpleRecipe<T extends RecipeInput> implements Recipe<T> {

	public boolean canCraftInDimensions(int width, int height) {
		return true;
	}

	@Override
	public ItemStack assemble(T input) {
		return ItemStack.EMPTY;
	}

	public ItemStack getResultItem() {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean showNotification() {
		return true;
	}

	@Override
	public String group() {
		return "";
	}

	@Override
	public PlacementInfo placementInfo() {
		return PlacementInfo.NOT_PLACEABLE;
	}

	@Override
	public RecipeBookCategory recipeBookCategory() {
		return RecipeBookCategories.CRAFTING_MISC;
	}

}
