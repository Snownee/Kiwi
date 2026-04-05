package snownee.kiwi.recipe.crafting;

import java.util.ArrayList;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import snownee.kiwi.data.DataModule;

public class KiwiShapelessRecipe extends CustomRecipe {

	private final String group;
	private final CraftingBookCategory category;
	private final ItemStack result;
	private final NonNullList<Ingredient> ingredients;
	private final boolean noContainers;
	private final boolean isSimple;
	private final PlacementInfo placementInfo;
	private boolean trimmed;

	public KiwiShapelessRecipe(
			String group,
			CraftingBookCategory category,
			ItemStack result,
			NonNullList<Ingredient> ingredients,
			boolean noContainers) {
		this.group = group;
		this.category = category;
		this.result = result;
		this.ingredients = mutableCopy(ingredients);
		this.noContainers = noContainers;
		trim();
		this.isSimple = this.ingredients.stream().allMatch(Ingredient::isSimple);
		this.placementInfo = PlacementInfo.create(this.ingredients);
	}

	private static NonNullList<Ingredient> mutableCopy(Iterable<Ingredient> ingredients) {
		NonNullList<Ingredient> copy = NonNullList.create();
		for (Ingredient ingredient : ingredients) {
			copy.add(ingredient);
		}
		return copy;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		if (input.ingredientCount() != ingredients.size()) {
			return false;
		} else if (!isSimple) {
			ArrayList<ItemStack> nonEmptyItems = new ArrayList<>(input.ingredientCount());
			for (ItemStack item : input.items()) {
				if (!item.isEmpty()) {
					nonEmptyItems.add(item);
				}
			}
			return net.neoforged.neoforge.common.util.RecipeMatcher.findMatches(nonEmptyItems, ingredients) != null;
		} else {
			return input.size() == 1 && ingredients.size() == 1 ? ingredients.getFirst().test(input.getItem(0)) : input.stackedContents().canCraft(this, null);
		}
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		return result.copy();
	}

	private void trim() {
		if (trimmed) {
			return;
		}
		trimmed = true;
		ingredients.removeIf(Ingredient::isEmpty);
	}

	public NonNullList<Ingredient> getIngredients() {
		return ingredients;
	}

	@Override
	public CraftingBookCategory category() {
		return category;
	}

	@Override
	public String group() {
		return group;
	}

	@Override
	public boolean isSpecial() {
		return false;
	}

	@Override
	public boolean showNotification() {
		return true;
	}

	@Override
	public PlacementInfo placementInfo() {
		return placementInfo;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
		if (noContainers) {
			return NonNullList.withSize(input.size(), ItemStack.EMPTY);
		}
		return super.getRemainingItems(input);
	}

	@Override
	public RecipeSerializer<KiwiShapelessRecipe> getSerializer() {
		return DataModule.SHAPELESS.get();
	}

	public ItemStack result() {
		return result;
	}

	public boolean noContainers() {
		return noContainers;
	}

	public static class Serializer {
		public static final MapCodec<KiwiShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
						Codec.STRING.optionalFieldOf("group", "").forGetter(KiwiShapelessRecipe::group),
						CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(KiwiShapelessRecipe::category),
						ItemStack.CODEC.fieldOf("result").forGetter(KiwiShapelessRecipe::result),
						Ingredient.CODEC.listOf().fieldOf("ingredients").flatXmap(list -> {
							NonNullList<Ingredient> ingredients = NonNullList.create();
							list.stream().filter(ingredient -> !ingredient.isEmpty()).forEach(ingredients::add);
							if (ingredients.isEmpty()) {
								return DataResult.error(() -> "No ingredients for shapeless recipe");
							}
							if (ingredients.size() > 9) {
								return DataResult.error(() -> "Too many ingredients for shapeless recipe");
							}
							return DataResult.success(ingredients);
						}, DataResult::success).forGetter(KiwiShapelessRecipe::getIngredients),
						Codec.BOOL.optionalFieldOf("no_containers", false).forGetter(KiwiShapelessRecipe::noContainers))
				.apply(i, KiwiShapelessRecipe::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, KiwiShapelessRecipe> STREAM_CODEC = StreamCodec.of(
				Serializer::toNetwork,
				Serializer::fromNetwork);

		public static KiwiShapelessRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
			String group = buffer.readUtf();
			CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
			int size = buffer.readVarInt();
			NonNullList<Ingredient> ingredients = NonNullList.create();
			for (int i = 0; i < size; i++) {
				ingredients.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
			}
			ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
			boolean noContainers = buffer.readBoolean();
			return new KiwiShapelessRecipe(group, category, result, ingredients, noContainers);
		}

		public static void toNetwork(RegistryFriendlyByteBuf buffer, KiwiShapelessRecipe recipe) {
			buffer.writeUtf(recipe.group);
			buffer.writeEnum(recipe.category);
			buffer.writeVarInt(recipe.ingredients.size());
			for (Ingredient ingredient : recipe.ingredients) {
				Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
			}
			ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
			buffer.writeBoolean(recipe.noContainers);
		}
	}
}
