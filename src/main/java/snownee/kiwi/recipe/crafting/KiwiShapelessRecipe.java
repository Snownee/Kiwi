package snownee.kiwi.recipe.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import snownee.kiwi.data.DataModule;

public class KiwiShapelessRecipe extends ShapelessRecipe {

	private final boolean noContainers;
	private boolean trimmed;

	public KiwiShapelessRecipe(String string, CraftingBookCategory craftingBookCategory, ItemStack itemStack, NonNullList<Ingredient> nonNullList, boolean noContainers) {
		super(string, craftingBookCategory, itemStack, mutableCopy(nonNullList));
		this.noContainers = noContainers;
	}

	public KiwiShapelessRecipe(ShapelessRecipe rawRecipe, boolean noContainers) {
		super(rawRecipe.getGroup(), rawRecipe.category(), rawRecipe.result, mutableCopy(rawRecipe.getIngredients()));
		this.noContainers = noContainers;
	}

	private static NonNullList<Ingredient> mutableCopy(NonNullList<Ingredient> list) {
		NonNullList<Ingredient> copy = NonNullList.create();
		copy.addAll(list);
		return copy;
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
		public static final Codec<KiwiShapelessRecipe> CODEC = RecordCodecBuilder.create(i -> i.group(
						ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter(ShapelessRecipe::getGroup),
						CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapelessRecipe::category),
						ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
						Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").flatXmap(list -> {
							Ingredient[] ingredients = list.stream().filter(ingredient -> !ingredient.isEmpty()).toArray(Ingredient[]::new);
							if (ingredients.length == 0) {
								return DataResult.error(() -> "No ingredients for shapeless recipe");
							} else {
								return ingredients.length > 9 ? DataResult.error(() -> "Too many ingredients for shapeless recipe") : DataResult.success(NonNullList.of(Ingredient.EMPTY, ingredients));
							}
						}, DataResult::success).forGetter(ShapelessRecipe::getIngredients),
						Codec.BOOL.optionalFieldOf("no_containers", false).forGetter(recipe -> recipe.noContainers))
				.apply(i, KiwiShapelessRecipe::new));

		@Override
		public KiwiShapelessRecipe fromNetwork(FriendlyByteBuf buffer) {
			return new KiwiShapelessRecipe(RecipeSerializer.SHAPELESS_RECIPE.fromNetwork(buffer), buffer.readBoolean());
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, KiwiShapelessRecipe recipe) {
			RecipeSerializer.SHAPELESS_RECIPE.toNetwork(buffer, recipe);
			buffer.writeBoolean(recipe.noContainers);
		}

		@Override
		public Codec<KiwiShapelessRecipe> codec() {
			return CODEC;
		}
	}
}
