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
import net.minecraft.world.level.Level;
import snownee.kiwi.data.DataModule;

public class KiwiShapelessRecipe extends ShapelessRecipe {

	private boolean noContainers;
	private boolean trimmed;

	public KiwiShapelessRecipe(ShapelessRecipe rawRecipe, boolean noContainers) {
		super(rawRecipe.getId(), rawRecipe.getGroup(), rawRecipe.category(), rawRecipe.getResultItem(null), rawRecipe.getIngredients());
		this.noContainers = noContainers;
	}

	@Override
	public boolean matches(CraftingContainer craftingContainer, Level level) {
		trim();
		return super.matches(craftingContainer, level);
	}

	@Override
	public boolean canCraftInDimensions(int i, int j) {
		trim();
		return super.canCraftInDimensions(i, j);
	}

	private void trim() {
		if (trimmed) {
			return;
		}
		trimmed = true;
		getIngredients().removeIf(Ingredient::isEmpty);
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
				if (ingredient != Ingredient.EMPTY) {
					nonnulllist.add(ingredient);
				}
			}

			return nonnulllist;
		}

		@Override
		public KiwiShapelessRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			String string = buffer.readUtf();
			CraftingBookCategory craftingBookCategory = buffer.readEnum(CraftingBookCategory.class);
			int i = buffer.readVarInt();
			NonNullList<Ingredient> nonNullList = NonNullList.createWithCapacity(i);
			for (int j = 0; j < i; ++j) {
				nonNullList.add(Ingredient.fromNetwork(buffer));
			}
			ItemStack itemStack = buffer.readItem();
			return new KiwiShapelessRecipe(new ShapelessRecipe(recipeId, string, craftingBookCategory, itemStack, nonNullList), buffer.readBoolean());
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, KiwiShapelessRecipe recipe) {
			buffer.writeUtf(recipe.getGroup());
			buffer.writeEnum(recipe.category());
			buffer.writeVarInt(recipe.getIngredients().size());
			for (Ingredient ingredient : recipe.getIngredients()) {
				ingredient.toNetwork(buffer);
			}
			buffer.writeItem(recipe.result);
			buffer.writeBoolean(recipe.noContainers);
		}
	}
}
