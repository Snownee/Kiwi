package snownee.kiwi.recipe.crafting;

import com.google.gson.JsonObject;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import snownee.kiwi.data.DataModule;

public class NoContainersShapedRecipe extends ShapedRecipe {
	public NoContainersShapedRecipe(ShapedRecipe rawRecipe) {
		super(rawRecipe.getId(), rawRecipe.getGroup(), rawRecipe.getRecipeWidth(), rawRecipe.getRecipeHeight(), rawRecipe.getIngredients(), rawRecipe.getResultItem());
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
		return NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return DataModule.SHAPED_NO_CONTAINERS.get();
	}

	public static class Serializer implements RecipeSerializer<NoContainersShapedRecipe> {
		@Override
		public NoContainersShapedRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			return new NoContainersShapedRecipe(RecipeSerializer.SHAPED_RECIPE.fromJson(recipeId, json));
		}

		@Override
		public NoContainersShapedRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			return new NoContainersShapedRecipe(RecipeSerializer.SHAPED_RECIPE.fromNetwork(recipeId, buffer));
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, NoContainersShapedRecipe recipe) {
			RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe);
		}
	}
}
