package snownee.kiwi.util;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;

public abstract class WrappedBlockGetter implements BlockAndTintGetter {

	protected BlockAndTintGetter delegate;

	public void setLevel(BlockAndTintGetter level) {
		delegate = level;
	}

	@Override
	@Nullable
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
	public CardinalLighting cardinalLighting() {
		return delegate.cardinalLighting();
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return delegate.getLightEngine();
	}

	@Override
	public int getBlockTint(BlockPos pos, ColorResolver colorResolver) {
		return delegate.getBlockTint(pos, colorResolver);
	}

	@Override
	public int getHeight() {
		return delegate.getHeight();
	}

	@Override
	public int getMinY() {
		return delegate.getMinY();
	}

}
