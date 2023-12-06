package snownee.kiwi.test;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import snownee.kiwi.block.IKiwiBlock;
import snownee.kiwi.block.def.SimpleBlockDefinition;
import snownee.kiwi.block.entity.RetextureBlockEntity;
import snownee.kiwi.recipe.FullBlockIngredient;

public class TexBlock extends BaseEntityBlock implements IKiwiBlock {

	public TexBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return null;
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

	@SuppressWarnings("deprecation")
	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.hasBlockEntity() && state.getBlock() != newState.getBlock()) {
			worldIn.removeBlockEntity(pos);
		}
		super.onRemove(state, worldIn, pos, newState, isMoving);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
		return new TexBlockEntity(p_153215_, p_153216_);
	}
}
