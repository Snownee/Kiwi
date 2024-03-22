package snownee.kiwi.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

public abstract class SimpleRecipe<C extends Container> implements Recipe<C> {

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return true;
	}

	@Override
	public ItemStack assemble(C inv, HolderLookup.Provider registryAccess) {
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider registryAccess) {
		return ItemStack.EMPTY;
	}

}
