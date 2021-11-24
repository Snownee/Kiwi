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
		super(rawRecipe.getId(), rawRecipe.getGroup(), rawRecipe.getRecipeOutput(), rawRecipe.getIngredients());
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
		return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return DataModule.SHAPELESS_NO_CONTAINERS;
	}

	public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<NoContainersShapelessRecipe> {
		@Override
		public NoContainersShapelessRecipe read(ResourceLocation recipeId, JsonObject json) {
			return new NoContainersShapelessRecipe(IRecipeSerializer.CRAFTING_SHAPELESS.read(recipeId, json));
		}

		@Override
		public NoContainersShapelessRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
			return new NoContainersShapelessRecipe(IRecipeSerializer.CRAFTING_SHAPELESS.read(recipeId, buffer));
		}

		@Override
		public void write(PacketBuffer buffer, NoContainersShapelessRecipe recipe) {
			IRecipeSerializer.CRAFTING_SHAPELESS.write(buffer, recipe);
		}
	}
}
