package snownee.kiwi.recipe;

import java.util.Arrays;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.Kiwi;

public class FullBlockIngredient implements CustomIngredient {
	public static final Serializer SERIALIZER = new Serializer();
	private static final Codec<FullBlockIngredient> CODEC = createCodec(Ingredient.CODEC);
	private static final Codec<FullBlockIngredient> CODEC_NONEMPTY = createCodec(Ingredient.CODEC_NONEMPTY);
	private final Ingredient example;

	public FullBlockIngredient(Ingredient example) {
		this.example = example;
	}

	private static Codec<FullBlockIngredient> createCodec(Codec<Ingredient> ingredientCodec) {
		return RecordCodecBuilder.create(instance -> instance.group(
				ingredientCodec.fieldOf("example").forGetter(i -> i.example)
		).apply(instance, FullBlockIngredient::new));
	}

	public static boolean isFullBlock(ItemStack stack) {
		if (!isTextureBlock(stack)) {
			return false;
		}
		Block block = Block.byItem(stack.getItem());
		BlockState state = block.defaultBlockState();
		try {
			if (Block.isShapeFullBlock(state.getOcclusionShape(null, BlockPos.ZERO)))
				return true;
		} catch (Throwable e) {
		}
		return false;
	}

	public static boolean isTextureBlock(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return false;
		}
		Block block = Block.byItem(stack.getItem());
		BlockState state = block.defaultBlockState();
		return state.isSolid() && state.getRenderShape() == RenderShape.MODEL;
	}

	@Override
	public boolean test(ItemStack stack) {
		return isFullBlock(stack);
	}

	@Override
	public List<ItemStack> getMatchingStacks() {
		return Arrays.asList(example.getItems());
	}

	@Override
	public boolean requiresTesting() {
		return true;
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	public static class Serializer implements CustomIngredientSerializer<FullBlockIngredient> {
		@Override
		public ResourceLocation getIdentifier() {
			return new ResourceLocation(Kiwi.ID, "full_block");
		}

		@Override
		public Codec<FullBlockIngredient> getCodec(boolean allowEmpty) {
			return allowEmpty ? CODEC : CODEC_NONEMPTY;
		}

		@Override
		public FullBlockIngredient read(FriendlyByteBuf buf) {
			return new FullBlockIngredient(Ingredient.fromNetwork(buf));
		}

		@Override
		public void write(FriendlyByteBuf buf, FullBlockIngredient ingredient) {
			ingredient.example.toNetwork(buf);
		}
	}
}
