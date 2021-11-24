package snownee.kiwi.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SimulationBlockGetter extends WrappedBlockGetter {

	private BlockEntity simulatedBlockEntity;
	private BlockPos simulatedPos;
	private boolean useSelfLight;
	private int globalLight = -1;

	public void setBlockEntity(BlockEntity blockEntity) {
		simulatedBlockEntity = blockEntity;
	}

	public void setPos(BlockPos pos) {
		simulatedPos = pos;
	}

	@Override
	public BlockEntity getBlockEntity(BlockPos pos) {
		if (simulatedBlockEntity != null && pos.equals(simulatedPos)) {
			return simulatedBlockEntity;
		}
		return super.getBlockEntity(pos);
	}

	public void useSelfLight(boolean useSelfLight) {
		this.useSelfLight = useSelfLight;
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		if (globalLight != -1 && !pos.equals(simulatedPos)) {
			return Blocks.AIR.defaultBlockState();
		}
		if (useSelfLight && simulatedPos != null) {
			if (simulatedPos.distManhattan(pos) < 3) {
				return Blocks.AIR.defaultBlockState();
			}
		}
		return super.getBlockState(pos);
	}

	@Override
	public int getBrightness(LightLayer lightType, BlockPos pos) {
		if (globalLight != -1) {
			return globalLight;
		}
		if (useSelfLight && simulatedPos != null) {
			if (simulatedPos.distManhattan(pos) < 3) {
				pos = simulatedPos;
			}
		}
		return super.getBrightness(lightType, pos);
	}

	public void setOverrideLight(int i) {
		globalLight = i;
	}

}
