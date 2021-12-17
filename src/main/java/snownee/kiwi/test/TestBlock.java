package snownee.kiwi.test;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import snownee.kiwi.block.IKiwiBlock;
import snownee.kiwi.block.def.SimpleBlockDefinition;
import snownee.kiwi.block.entity.RetextureBlockEntity;
import snownee.kiwi.recipe.FullBlockIngredient;

// fill ~-40 ~ ~-40 ~40 ~ ~40 kiwi:tex_block
public class TestBlock extends StairBlock implements EntityBlock, IKiwiBlock {

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
				textureTile.setTexture("0", SimpleBlockDefinition.of(state2));
				textureTile.refresh();
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean isMoving) {
		if (pState.hasBlockEntity() && (!pState.is(pNewState.getBlock()) || !pNewState.hasBlockEntity())) {
			pLevel.removeBlockEntity(pPos);
		}
		super.onRemove(pState, pLevel, pPos, pNewState, isMoving);
	}

	//	@Override
	//	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
	//		return ModBlock.pick(state, target, world, pos, player);
	//	}

	@Override
	public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
		return new TestBlockEntity(p_153215_, p_153216_);
	}

	@Override
	public MutableComponent getName(ItemStack stack) {
		return IKiwiBlock.super.getName(stack).append(" " + stack.getCount());
	}
}
