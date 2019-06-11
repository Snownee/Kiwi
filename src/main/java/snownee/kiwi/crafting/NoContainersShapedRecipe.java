package snownee.kiwi.crafting;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

// inherited from ShapedRecipe for better compatibility with JEI
public class NoContainersShapedRecipe extends ShapedRecipe
{

    public NoContainersShapedRecipe(ResourceLocation idIn, String groupIn, int recipeWidthIn, int recipeHeightIn, NonNullList<Ingredient> recipeItemsIn, ItemStack recipeOutputIn)
    {
        super(idIn, groupIn, recipeWidthIn, recipeHeightIn, recipeItemsIn, recipeOutputIn);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv)
    {
        return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
    }

    static Field WIDTH;
    static Field HEIGHT;

    static
    {
        try
        {
            WIDTH = ShapedRecipe.class.getDeclaredField("MAX_WIDTH");
            WIDTH.setAccessible(true);
            HEIGHT = ShapedRecipe.class.getDeclaredField("MAX_HEIGHT");
            HEIGHT.setAccessible(true);
        }
        catch (NullPointerException | NoSuchFieldException | SecurityException e)
        {
            e.printStackTrace();
        }
    }

    private static Map<String, Ingredient> deserializeKey(JsonObject json)
    {
        Map<String, Ingredient> map = Maps.newHashMap();

        for (Entry<String, JsonElement> entry : json.entrySet())
        {
            if (entry.getKey().length() != 1)
            {
                throw new JsonSyntaxException("Invalid key entry: '" + (String) entry.getKey() + "' is an invalid symbol (must be 1 character only).");
            }

            if (" ".equals(entry.getKey()))
            {
                throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
            }

            map.put(entry.getKey(), Ingredient.deserialize(entry.getValue()));
        }

        map.put(" ", Ingredient.EMPTY);
        return map;
    }

    private static String[] shrink(String... toShrink)
    {
        int i = Integer.MAX_VALUE;
        int j = 0;
        int k = 0;
        int l = 0;

        for (int i1 = 0; i1 < toShrink.length; ++i1)
        {
            String s = toShrink[i1];
            i = Math.min(i, firstNonSpace(s));
            int j1 = lastNonSpace(s);
            j = Math.max(j, j1);
            if (j1 < 0)
            {
                if (k == i1)
                {
                    ++k;
                }

                ++l;
            }
            else
            {
                l = 0;
            }
        }

        if (toShrink.length == l)
        {
            return new String[0];
        }
        else
        {
            String[] astring = new String[toShrink.length - l - k];

            for (int k1 = 0; k1 < astring.length; ++k1)
            {
                astring[k1] = toShrink[k1 + k].substring(i, j + 1);
            }

            return astring;
        }
    }

    private static int firstNonSpace(String str)
    {
        int i;
        for (i = 0; i < str.length() && str.charAt(i) == ' '; ++i)
        {
            ;
        }

        return i;
    }

    private static int lastNonSpace(String str)
    {
        int i;
        for (i = str.length() - 1; i >= 0 && str.charAt(i) == ' '; --i)
        {
            ;
        }

        return i;
    }

    private static String[] patternFromJson(JsonArray jsonArr)
    {
        int maxHeight = 3;
        int maxWidth = 3;
        try
        {
            maxHeight = (int) HEIGHT.get(null);
            maxWidth = (int) WIDTH.get(null);
        }
        catch (NullPointerException | IllegalArgumentException | IllegalAccessException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String[] astring = new String[jsonArr.size()];
        if (astring.length > maxHeight)
        {
            throw new JsonSyntaxException("Invalid pattern: too many rows, " + maxHeight + " is maximum");
        }
        else if (astring.length == 0)
        {
            throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
        }
        else
        {
            for (int i = 0; i < astring.length; ++i)
            {
                String s = JSONUtils.getString(jsonArr.get(i), "pattern[" + i + "]");
                if (s.length() > maxWidth)
                {
                    throw new JsonSyntaxException("Invalid pattern: too many columns, " + maxWidth + " is maximum");
                }

                if (i > 0 && astring[0].length() != s.length())
                {
                    throw new JsonSyntaxException("Invalid pattern: each row must be the same width");
                }

                astring[i] = s;
            }

            return astring;
        }
    }

    private static NonNullList<Ingredient> deserializeIngredients(String[] pattern, Map<String, Ingredient> keys, int patternWidth, int patternHeight)
    {
        NonNullList<Ingredient> nonnulllist = NonNullList.withSize(patternWidth * patternHeight, Ingredient.EMPTY);
        Set<String> set = Sets.newHashSet(keys.keySet());
        set.remove(" ");

        for (int i = 0; i < pattern.length; ++i)
        {
            for (int j = 0; j < pattern[i].length(); ++j)
            {
                String s = pattern[i].substring(j, j + 1);
                Ingredient ingredient = keys.get(s);
                if (ingredient == null)
                {
                    throw new JsonSyntaxException("Pattern references symbol '" + s + "' but it's not defined in the key");
                }

                set.remove(s);
                nonnulllist.set(j + patternWidth * i, ingredient);
            }
        }

        if (!set.isEmpty())
        {
            throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + set);
        }
        else
        {
            return nonnulllist;
        }
    }

    public static class Serializer extends net.minecraftforge.registries.ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<NoContainersShapedRecipe>
    {
        @Override
        public NoContainersShapedRecipe read(ResourceLocation recipeId, JsonObject json)
        {
            String s = JSONUtils.getString(json, "group", "");
            Map<String, Ingredient> map = NoContainersShapedRecipe.deserializeKey(JSONUtils.getJsonObject(json, "key"));
            String[] astring = NoContainersShapedRecipe.shrink(NoContainersShapedRecipe.patternFromJson(JSONUtils.getJsonArray(json, "pattern")));
            int i = astring[0].length();
            int j = astring.length;
            NonNullList<Ingredient> nonnulllist = NoContainersShapedRecipe.deserializeIngredients(astring, map, i, j);
            ItemStack itemstack = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "result"));
            return new NoContainersShapedRecipe(recipeId, s, i, j, nonnulllist, itemstack);
        }

        @Override
        public NoContainersShapedRecipe read(ResourceLocation recipeId, PacketBuffer buffer)
        {
            int i = buffer.readVarInt();
            int j = buffer.readVarInt();
            String s = buffer.readString(32767);
            NonNullList<Ingredient> nonnulllist = NonNullList.withSize(i * j, Ingredient.EMPTY);

            for (int k = 0; k < nonnulllist.size(); ++k)
            {
                nonnulllist.set(k, Ingredient.read(buffer));
            }

            ItemStack itemstack = buffer.readItemStack();
            return new NoContainersShapedRecipe(recipeId, s, i, j, nonnulllist, itemstack);
        }

        @Override
        public void write(PacketBuffer buffer, NoContainersShapedRecipe recipe)
        {
            buffer.writeVarInt(recipe.getRecipeWidth());
            buffer.writeVarInt(recipe.getRecipeHeight());
            buffer.writeString(recipe.getGroup());

            for (Ingredient ingredient : recipe.getIngredients())
            {
                ingredient.write(buffer);
            }

            buffer.writeItemStack(recipe.getRecipeOutput());
        }
    }
}
