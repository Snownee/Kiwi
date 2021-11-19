package snownee.kiwi.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class WrappedBlockReader implements BlockAndTintGetter {

	protected BlockAndTintGetter delegate;

	public void setLevel(BlockAndTintGetter level) {
		delegate = level;
	}

	@Override
	public BlockEntity getBlockEntity(BlockPos pos) {
		return delegate.getBlockEntity(pos);
	}

	@Override
	public BlockState getBlockState(BlockPos p_180495_1_) {
		return delegate.getBlockState(p_180495_1_);
	}

	@Override
	public FluidState getFluidState(BlockPos p_204610_1_) {
		return delegate.getFluidState(p_204610_1_);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public float getShade(Direction p_230487_1_, boolean p_230487_2_) {
		return delegate.getShade(p_230487_1_, p_230487_2_);
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return delegate.getLightEngine();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public int getBlockTint(BlockPos pos, ColorResolver colorResolver) {
		return delegate.getBlockTint(pos, colorResolver);
	}

	@Override
	public int getHeight() {
		return delegate.getHeight();
	}

	@Override
	public int getMinBuildHeight() {
		return delegate.getMinBuildHeight();
	}

}
