package snownee.kiwi.customization.block;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public interface CheckedWaterloggedBlock extends SimpleWaterloggedBlock {
	@Override
	default boolean canPlaceLiquid(@Nullable LivingEntity entity, BlockGetter pLevel, BlockPos pPos, BlockState pState, Fluid pFluid) {
		return pState.hasProperty(BlockStateProperties.WATERLOGGED) && SimpleWaterloggedBlock.super.canPlaceLiquid(
				entity,
				pLevel,
				pPos,
				pState,
				pFluid);
	}

	@Override
	default boolean placeLiquid(LevelAccessor pLevel, BlockPos pPos, BlockState pState, FluidState pFluidState) {
		return pState.hasProperty(BlockStateProperties.WATERLOGGED) && SimpleWaterloggedBlock.super.placeLiquid(
				pLevel,
				pPos,
				pState,
				pFluidState);
	}

	@Override
	default ItemStack pickupBlock(@Nullable LivingEntity entity, LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
		if (!pState.hasProperty(BlockStateProperties.WATERLOGGED)) {
			return ItemStack.EMPTY;
		}
		return SimpleWaterloggedBlock.super.pickupBlock(entity, pLevel, pPos, pState);
	}
}