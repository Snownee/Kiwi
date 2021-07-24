package snownee.kiwi.crafting;

import com.google.gson.JsonObject;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import snownee.kiwi.data.DataModule;

public class NoContainersShapelessRecipe extends ShapelessRecipe {
	public NoContainersShapelessRecipe(ShapelessRecipe rawRecipe) {
		super(rawRecipe.getId(), rawRecipe.getGroup(), rawRecipe.getResultItem(), rawRecipe.getIngredients());
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
		return NonNullList.create();
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return DataModule.SHAPELESS_NO_CONTAINERS;
	}

	public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<NoContainersShapelessRecipe> {
		@Override
		public NoContainersShapelessRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			return new NoContainersShapelessRecipe(IRecipeSerializer.SHAPELESS_RECIPE.fromJson(recipeId, json));
		}

		@Override
		public NoContainersShapelessRecipe fromNetwork(ResourceLocation recipeId, PacketBuffer buffer) {
			return new NoContainersShapelessRecipe(IRecipeSerializer.SHAPELESS_RECIPE.fromNetwork(recipeId, buffer));
		}

		@Override
		public void toNetwork(PacketBuffer buffer, NoContainersShapelessRecipe recipe) {
			IRecipeSerializer.SHAPELESS_RECIPE.toNetwork(buffer, recipe);
		}
	}
}
