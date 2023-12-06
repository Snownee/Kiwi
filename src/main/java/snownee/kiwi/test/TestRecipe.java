package snownee.kiwi.test;

import com.google.gson.JsonObject;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import snownee.kiwi.recipe.crafting.DynamicShapedRecipe;

public class TestRecipe extends DynamicShapedRecipe {

	public TestRecipe(CraftingBookCategory category) {
		super(category);
	}

	// optional
	@Override
	public boolean matches(CraftingContainer inv, Level worldIn) {
		int[] pos = search(inv);
		if (pos == null) {
			return false;
		}
		ItemStack stack = item('#', inv, pos);
		return stack.hasTag() && stack.getTag().contains("Rarity");
	}

	@Override
	public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
		ItemStack res = recipeOutput.copy();
		int[] pos = search(inv);
		ItemStack stack = item('#', inv, pos);
		if ("SSR".equals(stack.getTag().getString("Rarity"))) {
			res.grow(res.getCount());
		}
		return res;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return null; //TODO your serializer
	}

	public static class Serializer extends DynamicShapedRecipe.Serializer<TestRecipe> {

		@Override
		public TestRecipe fromJson(JsonObject pSerializedRecipe) {
			//TODO customize recipe
			return fromJson(TestRecipe::new, pSerializedRecipe);
		}

		@Override
		public void toJson(JsonObject json, TestRecipe recipe) {
			throw new UnsupportedOperationException();
		}

		@Override
		public TestRecipe fromNetwork(FriendlyByteBuf pBuffer) {
			//TODO customize recipe
			return fromNetwork(TestRecipe::new, pBuffer);
		}

	}
}
