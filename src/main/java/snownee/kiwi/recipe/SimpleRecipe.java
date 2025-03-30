package snownee.kiwi.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import snownee.kiwi.util.NotNullByDefault;

@NotNullByDefault
public abstract class SimpleRecipe<T extends RecipeInput> implements Recipe<T> {

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return true;
	}

	@Override
	public ItemStack assemble(T input, HolderLookup.Provider registryAccess) {
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider registryAccess) {
		return ItemStack.EMPTY;
	}

}
