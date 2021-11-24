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
		super(rawRecipe.getId(), rawRecipe.getGroup(), rawRecipe.getRecipeWidth(), rawRecipe.getRecipeHeight(), rawRecipe.getIngredients(), rawRecipe.getRecipeOutput());
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
		return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return DataModule.SHAPED_NO_CONTAINERS;
	}

	public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<NoContainersShapedRecipe> {
		@Override
		public NoContainersShapedRecipe read(ResourceLocation recipeId, JsonObject json) {
			return new NoContainersShapedRecipe(IRecipeSerializer.CRAFTING_SHAPED.read(recipeId, json));
		}

		@Override
		public NoContainersShapedRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
			return new NoContainersShapedRecipe(IRecipeSerializer.CRAFTING_SHAPED.read(recipeId, buffer));
		}

		@Override
		public void write(PacketBuffer buffer, NoContainersShapedRecipe recipe) {
			IRecipeSerializer.CRAFTING_SHAPED.write(buffer, recipe);
		}
	}
}
