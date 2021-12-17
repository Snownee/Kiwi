package snownee.kiwi.recipe.crafting;

import com.google.gson.JsonObject;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import snownee.kiwi.data.DataModule;

public class NoContainersShapelessRecipe extends ShapelessRecipe {
	public NoContainersShapelessRecipe(ShapelessRecipe rawRecipe) {
		super(rawRecipe.getId(), rawRecipe.getGroup(), rawRecipe.getResultItem(), rawRecipe.getIngredients());
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
		return NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return DataModule.SHAPELESS_NO_CONTAINERS;
	}

	public static class Serializer implements RecipeSerializer<NoContainersShapelessRecipe> {
		@Override
		public NoContainersShapelessRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			return new NoContainersShapelessRecipe(RecipeSerializer.SHAPELESS_RECIPE.fromJson(recipeId, json));
		}

		@Override
		public NoContainersShapelessRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			return new NoContainersShapelessRecipe(RecipeSerializer.SHAPELESS_RECIPE.fromNetwork(recipeId, buffer));
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, NoContainersShapelessRecipe recipe) {
			RecipeSerializer.SHAPELESS_RECIPE.toNetwork(buffer, recipe);
		}
	}
}
