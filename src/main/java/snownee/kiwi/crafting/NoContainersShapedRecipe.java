package snownee.kiwi.crafting;

import com.google.gson.JsonObject;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import snownee.kiwi.data.DataModule;

public class NoContainersShapedRecipe extends ShapedRecipe {
	public NoContainersShapedRecipe(ShapedRecipe rawRecipe) {
		super(rawRecipe.getId(), rawRecipe.getGroup(), rawRecipe.getRecipeWidth(), rawRecipe.getRecipeHeight(), rawRecipe.getIngredients(), rawRecipe.getResultItem());
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
		return NonNullList.create();
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return DataModule.SHAPED_NO_CONTAINERS;
	}

	public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<NoContainersShapedRecipe> {
		@Override
		public NoContainersShapedRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			return new NoContainersShapedRecipe(IRecipeSerializer.SHAPED_RECIPE.fromJson(recipeId, json));
		}

		@Override
		public NoContainersShapedRecipe fromNetwork(ResourceLocation recipeId, PacketBuffer buffer) {
			return new NoContainersShapedRecipe(IRecipeSerializer.SHAPED_RECIPE.fromNetwork(recipeId, buffer));
		}

		@Override
		public void toNetwork(PacketBuffer buffer, NoContainersShapedRecipe recipe) {
			IRecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe);
		}
	}
}
