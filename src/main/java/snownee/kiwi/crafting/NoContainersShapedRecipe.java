package snownee.kiwi.crafting;

import com.google.gson.JsonObject;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import snownee.kiwi.KiwiManager;

public class NoContainersShapedRecipe extends ShapedRecipe
{
    public NoContainersShapedRecipe(ShapedRecipe rawRecipe)
    {
        super(rawRecipe.getId(), rawRecipe.getGroup(), rawRecipe.getRecipeWidth(), rawRecipe.getRecipeHeight(), rawRecipe.getIngredients(), rawRecipe.getRecipeOutput());
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv)
    {
        return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
    }

    @Override
    public IRecipeSerializer<?> getSerializer()
    {
        return KiwiManager.shapedSerializer;
    }

    public static class Serializer extends net.minecraftforge.registries.ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<NoContainersShapedRecipe>
    {
        @Override
        public NoContainersShapedRecipe read(ResourceLocation recipeId, JsonObject json)
        {
            return new NoContainersShapedRecipe(IRecipeSerializer.field_222157_a.read(recipeId, json));
        }

        @Override
        public NoContainersShapedRecipe read(ResourceLocation recipeId, PacketBuffer buffer)
        {
            return new NoContainersShapedRecipe(IRecipeSerializer.field_222157_a.read(recipeId, buffer));
        }

        @Override
        public void write(PacketBuffer buffer, NoContainersShapedRecipe recipe)
        {
            IRecipeSerializer.field_222157_a.write(buffer, recipe);
        }
    }
}
