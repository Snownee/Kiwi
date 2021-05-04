package snownee.kiwi.crafting;

import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.common.crafting.StackList;

public class FullBlockIngredient extends Ingredient {
	public static final Serializer SERIALIZER = new Serializer();

	private final Ingredient example;

	protected FullBlockIngredient(Stream<? extends IItemList> itemLists, Ingredient example) {
		super(itemLists);
		this.example = example;
	}

	public static boolean isFullBlock(ItemStack stack) {
		if (!isTextureBlock(stack)) {
			return false;
		}
		Block block = Block.getBlockFromItem(stack.getItem());
		BlockState state = block.getDefaultState();
		boolean flag = state.isSolid() && !state.isTransparent();
		if (flag) {
			try {
				if (VoxelShapes.fullCube().equals(state.getCollisionShape(null, BlockPos.ZERO)))
					return true;
			} catch (Exception e) {
			}
		}
		return false;
	}

	public static boolean isTextureBlock(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return false;
		}
		Block block = Block.getBlockFromItem(stack.getItem());
		BlockState state = block.getDefaultState();
		return state.getMaterial().isSolid() && state.getRenderType() == BlockRenderType.MODEL;
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
	public boolean hasNoMatchingItems() {
		return false;
	}

	@Override
	public boolean isSimple() {
		return false;
	}

	public static class Serializer implements IIngredientSerializer<FullBlockIngredient> {

		@Override
		public FullBlockIngredient parse(PacketBuffer buffer) {
			Ingredient example = Ingredient.read(buffer);
			StackList stackList = new StackList(ImmutableList.copyOf(example.getMatchingStacks()));
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
			StackList stackList = new StackList(ImmutableList.copyOf(example.getMatchingStacks()));
			return new FullBlockIngredient(Stream.of(stackList), example);
		}

		@Override
		public void write(PacketBuffer buffer, FullBlockIngredient ingredient) {
			ingredient.example.write(buffer);
		}

	}

}
