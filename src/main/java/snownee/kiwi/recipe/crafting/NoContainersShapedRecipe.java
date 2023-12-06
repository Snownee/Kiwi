package snownee.kiwi.recipe.crafting;

import com.mojang.serialization.Codec;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import snownee.kiwi.data.DataModule;

public class NoContainersShapedRecipe extends ShapedRecipe {
	public NoContainersShapedRecipe(ShapedRecipe rawRecipe) {
		super(rawRecipe.getGroup(), rawRecipe.category(), rawRecipe.pattern, rawRecipe.result, rawRecipe.showNotification());
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
		public Codec<NoContainersShapedRecipe> codec() {
			return RecipeSerializer.SHAPED_RECIPE.codec().xmap(NoContainersShapedRecipe::new, recipe -> recipe);
		}

		@Override
		public NoContainersShapedRecipe fromNetwork(FriendlyByteBuf buffer) {
			return new NoContainersShapedRecipe(RecipeSerializer.SHAPED_RECIPE.fromNetwork(buffer));
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, NoContainersShapedRecipe recipe) {
			RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe);
		}
	}
}
