package snownee.kiwi.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

public class NoContainersShapelessRecipe extends ShapelessRecipe
{

    public NoContainersShapelessRecipe(ResourceLocation idIn, String groupIn, ItemStack recipeOutputIn, NonNullList<Ingredient> recipeItemsIn)
    {
        super(idIn, groupIn, recipeOutputIn, recipeItemsIn);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv)
    {
        return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
    }

    public static class Serializer extends net.minecraftforge.registries.ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<NoContainersShapelessRecipe>
    {
        @Override
        public NoContainersShapelessRecipe read(ResourceLocation recipeId, JsonObject json)
        {
            int maxHeight = 3;
            int maxWidth = 3;
            try
            {
                maxHeight = (int) NoContainersShapedRecipe.HEIGHT.get(null);
                maxWidth = (int) NoContainersShapedRecipe.WIDTH.get(null);
            }
            catch (NullPointerException | IllegalArgumentException | IllegalAccessException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            String s = JSONUtils.getString(json, "group", "");
            NonNullList<Ingredient> nonnulllist = readIngredients(JSONUtils.getJsonArray(json, "ingredients"));
            if (nonnulllist.isEmpty())
            {
                throw new JsonParseException("No ingredients for shapeless recipe");
            }
            else if (nonnulllist.size() > maxWidth * maxHeight)
            {
                throw new JsonParseException("Too many ingredients for shapeless recipe the max is " + (maxWidth * maxHeight));
            }
            else
            {
                ItemStack itemstack = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "result"));
                return new NoContainersShapelessRecipe(recipeId, s, itemstack, nonnulllist);
            }
        }

        private static NonNullList<Ingredient> readIngredients(JsonArray p_199568_0_)
        {
            NonNullList<Ingredient> nonnulllist = NonNullList.create();

            for (int i = 0; i < p_199568_0_.size(); ++i)
            {
                Ingredient ingredient = Ingredient.deserialize(p_199568_0_.get(i));
                if (!ingredient.hasNoMatchingItems())
                {
                    nonnulllist.add(ingredient);
                }
            }

            return nonnulllist;
        }

        @Override
        public NoContainersShapelessRecipe read(ResourceLocation recipeId, PacketBuffer buffer)
        {
            String s = buffer.readString(32767);
            int i = buffer.readVarInt();
            NonNullList<Ingredient> nonnulllist = NonNullList.withSize(i, Ingredient.EMPTY);

            for (int j = 0; j < nonnulllist.size(); ++j)
            {
                nonnulllist.set(j, Ingredient.read(buffer));
            }

            ItemStack itemstack = buffer.readItemStack();
            return new NoContainersShapelessRecipe(recipeId, s, itemstack, nonnulllist);
        }

        @Override
        public void write(PacketBuffer buffer, NoContainersShapelessRecipe recipe)
        {
            buffer.writeString(recipe.getGroup());
            buffer.writeVarInt(recipe.getIngredients().size());

            for (Ingredient ingredient : recipe.getIngredients())
            {
                ingredient.write(buffer);
            }

            buffer.writeItemStack(recipe.getRecipeOutput());
        }
    }
}
