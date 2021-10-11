package snownee.kiwi.test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import snownee.kiwi.RenderLayer;
import snownee.kiwi.RenderLayer.Layer;
import snownee.kiwi.block.ModBlock;
import snownee.kiwi.block.entity.RetextureBlockEntity;
import snownee.kiwi.crafting.FullBlockIngredient;
import snownee.kiwi.util.BlockStateBlockDefinition;

// fill ~-40 ~ ~-40 ~40 ~ ~40 kiwi:tex_block
@RenderLayer(Layer.CUTOUT)
public class TestBlock extends StairBlock implements EntityBlock {

	@SuppressWarnings("deprecation")
	public TestBlock(Properties builder) {
		super(Blocks.STONE.defaultBlockState(), builder);
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
		if (worldIn.isClientSide) {
			return InteractionResult.SUCCESS;
		}
		BlockEntity tile = worldIn.getBlockEntity(pos);
		ItemStack stack = player.getItemInHand(handIn);
		if (tile instanceof RetextureBlockEntity && !stack.isEmpty()) {
			if (FullBlockIngredient.isFullBlock(stack)) {
				RetextureBlockEntity textureTile = (RetextureBlockEntity) tile;
				BlockState state2 = ((BlockItem) stack.getItem()).getBlock().defaultBlockState();
				textureTile.setTexture("0", BlockStateBlockDefinition.of(state2));
				textureTile.refresh();
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.hasBlockEntity() && state.getBlock() != newState.getBlock()) {
			worldIn.removeBlockEntity(pos);
		}
		super.onRemove(state, worldIn, pos, newState, isMoving);
	}

	@Override
	public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
		return ModBlock.pickBlock(state, target, world, pos, player);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
		return new TestBlockEntity(p_153215_, p_153216_);
	}
}
