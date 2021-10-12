package snownee.kiwi.crafting;

import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.common.crafting.StackList;

public class FullBlockIngredient extends Ingredient {
	public static final Serializer SERIALIZER = new Serializer();

	private final Ingredient example;

	protected FullBlockIngredient(Stream<? extends Ingredient.Value> itemLists, Ingredient example) {
		super(itemLists);
		this.example = example;
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
		return state.getMaterial().isSolid() && state.getRenderShape() == RenderShape.MODEL;
	}

	@Override
	public boolean test(ItemStack stack) {
		return isFullBlock(stack);
	}

	@Override
	public Serializer getSerializer() {
		return SERIALIZER;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean isSimple() {
		return false;
	}

	public static class Serializer implements IIngredientSerializer<FullBlockIngredient> {

		@Override
		public FullBlockIngredient parse(FriendlyByteBuf buffer) {
			Ingredient example = Ingredient.fromNetwork(buffer);
			StackList stackList = new StackList(ImmutableList.copyOf(example.getItems()));
			return new FullBlockIngredient(Stream.of(stackList), example);
		}

		@Override
		public FullBlockIngredient parse(JsonObject json) {
			Ingredient example;
			try {
				example = CraftingHelper.getIngredient(json.get("example"));
			} catch (JsonSyntaxException e) {
				example = Ingredient.EMPTY;
			}
			StackList stackList = new StackList(ImmutableList.copyOf(example.getItems()));
			return new FullBlockIngredient(Stream.of(stackList), example);
		}

		@Override
		public void write(FriendlyByteBuf buffer, FullBlockIngredient ingredient) {
			ingredient.example.toNetwork(buffer);
		}

	}

}
