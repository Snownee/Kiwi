package snownee.kiwi.recipe.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import snownee.kiwi.data.DataModule;

public class KiwiShapelessRecipe extends ShapelessRecipe {

	private boolean noContainers;

	public KiwiShapelessRecipe(ShapelessRecipe rawRecipe, boolean noContainers) {
		super(rawRecipe.getId(), rawRecipe.getGroup(), rawRecipe.category(), rawRecipe.getResultItem(null), rawRecipe.getIngredients());
		this.noContainers = noContainers;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
		if (noContainers) {
			return NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
		} else {
			return super.getRemainingItems(inv);
		}
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return DataModule.SHAPELESS.get();
	}

	public static class Serializer implements RecipeSerializer<KiwiShapelessRecipe> {

		@Override
		public KiwiShapelessRecipe fromJson(ResourceLocation id, JsonObject o) {
			String s = GsonHelper.getAsString(o, "group", "");
			NonNullList<Ingredient> nonnulllist = itemsFromJson(GsonHelper.getAsJsonArray(o, "ingredients"));
			if (nonnulllist.isEmpty()) {
				throw new JsonParseException("No ingredients for shapeless recipe");
				//		} else if (nonnulllist.size() > ShapedRecipe.MAX_WIDTH * ShapedRecipe.MAX_HEIGHT) {
				//			throw new JsonParseException("Too many ingredients for shapeless recipe. The maximum is " + (ShapedRecipe.MAX_WIDTH * ShapedRecipe.MAX_HEIGHT));
			} else {
				@SuppressWarnings("deprecation")
				CraftingBookCategory category = CraftingBookCategory.CODEC.byName(GsonHelper.getAsString(o, "category", null), CraftingBookCategory.MISC);
				ItemStack itemstack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(o, "result"));
				return new KiwiShapelessRecipe(new ShapelessRecipe(id, s, category, itemstack, nonnulllist), GsonHelper.getAsBoolean(o, "no_containers", false));
			}
		}

		private static NonNullList<Ingredient> itemsFromJson(JsonArray a) {
			NonNullList<Ingredient> nonnulllist = NonNullList.create();

			for (int i = 0; i < a.size(); ++i) {
				Ingredient ingredient = Ingredient.fromJson(a.get(i));
				if (!ingredient.isEmpty()) {
					nonnulllist.add(ingredient);
				}
			}

			return nonnulllist;
		}

		@Override
		public KiwiShapelessRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			return new KiwiShapelessRecipe(RecipeSerializer.SHAPELESS_RECIPE.fromNetwork(recipeId, buffer), buffer.readBoolean());
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, KiwiShapelessRecipe recipe) {
			RecipeSerializer.SHAPELESS_RECIPE.toNetwork(buffer, recipe);
			buffer.writeBoolean(recipe.noContainers);
		}
	}
}
