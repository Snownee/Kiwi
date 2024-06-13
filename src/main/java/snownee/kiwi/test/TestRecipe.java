package snownee.kiwi.test;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
import snownee.kiwi.recipe.crafting.DynamicShapedRecipe;

public class TestRecipe extends DynamicShapedRecipe {

	public TestRecipe(
			String group,
			CraftingBookCategory category,
			ShapedRecipePattern pattern,
			ItemStack result,
			boolean showNotification,
			boolean differentInputs) {
		super(group, category, pattern, result, showNotification, differentInputs);
	}

	public TestRecipe(CraftingBookCategory category) {
		super(category);
	}

	// optional
	@Override
	public boolean matches(CraftingInput input, Level worldIn) {
		int[] pos = search(input);
		if (pos == null) {
			return false;
		}
		ItemStack stack = item('#', input, pos);
		CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
		return customData.contains("Rarity");
	}

	@Override
	public ItemStack assemble(CraftingInput input, HolderLookup.Provider registryAccess) {
		ItemStack res = result.copy();
		int[] pos = search(input);
		ItemStack stack = item('#', input, pos);
		CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
		if ("SSR".equals(customData.copyTag().getString("Rarity"))) {
			res.grow(res.getCount());
		}
		return res;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return null; //TODO your serializer
	}

	public static class Serializer extends DynamicShapedRecipe.Serializer<TestRecipe> {

		public static final MapCodec<TestRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.STRING.optionalFieldOf("group", "").forGetter(DynamicShapedRecipe::getGroup),
				CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(DynamicShapedRecipe::category),
				ShapedRecipePattern.MAP_CODEC.forGetter(DynamicShapedRecipe::pattern),
				ItemStack.CODEC.fieldOf("result").forGetter(DynamicShapedRecipe::result),
				Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(DynamicShapedRecipe::showNotification),
				Codec.BOOL.fieldOf("different_inputs").orElse(false).forGetter(TestRecipe::allowDifferentInputs)
		).apply(instance, TestRecipe::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, TestRecipe> STREAM_CODEC = StreamCodec.of(
				Serializer::toNetwork,
				Serializer::fromNetwork);

		@Override
		public MapCodec<TestRecipe> codec() {
			return CODEC;
		}

		public StreamCodec<RegistryFriendlyByteBuf, TestRecipe> streamCodec() {
			return STREAM_CODEC;
		}

		public static TestRecipe fromNetwork(RegistryFriendlyByteBuf pBuffer) {
			//TODO customize recipe
			return DynamicShapedRecipe.Serializer.fromNetwork(TestRecipe::new, pBuffer);
		}

		public static void toNetwork(RegistryFriendlyByteBuf pBuffer, TestRecipe pRecipe) {
			//TODO customize recipe
			DynamicShapedRecipe.Serializer.toNetwork(pBuffer, pRecipe);
		}
	}
}
