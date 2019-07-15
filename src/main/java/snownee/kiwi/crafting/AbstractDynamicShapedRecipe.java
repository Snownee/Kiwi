package snownee.kiwi.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;

abstract public class AbstractDynamicShapedRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IShapedRecipe
{
    @Override
    public boolean matches(InventoryCrafting inv, World world)
    {
        for (int x = 0; x <= inv.getWidth() - getRecipeWidth(); ++x)
        {
            for (int y = 0; y <= inv.getHeight() - getRecipeHeight(); ++y)
            {
                if (checkMatch(inv, x, y) && checkEmpty(inv, x, y))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public abstract ItemStack getCraftingResult(InventoryCrafting inv);

    @Override
    public boolean canFit(int width, int height)
    {
        return width >= getRecipeWidth() && height >= getRecipeHeight();
    }

    @Nonnull
    @Override
    public ItemStack getRecipeOutput()
    {
        return ItemStack.EMPTY;
    }

    @Override
    public abstract int getRecipeWidth();

    @Override
    public abstract int getRecipeHeight();

    protected abstract boolean checkMatch(InventoryCrafting inv, int startX, int startY);

    @Override
    public boolean isDynamic()
    {
        return true;
    }

    protected boolean checkEmpty(InventoryCrafting inv, int startX, int startY)
    {
        for (int x = 0; x < inv.getWidth(); ++x)
        {
            for (int y = 0; y < inv.getHeight(); ++y)
            {
                int subX = x - startX;
                int subY = y - startY;
                if (subX >= 0 && subY >= 0 && subX < this.getRecipeWidth() && subY < this.getRecipeHeight())
                {
                    continue;
                }

                if (!Ingredient.EMPTY.apply(inv.getStackInRowAndColumn(x, y)))
                {
                    return false;
                }
            }
        }

        return true;
    }
}
