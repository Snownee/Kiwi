package snownee.kiwi.test;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.block.entity.RetextureBlockEntity;

public class TexBlockEntity extends RetextureBlockEntity {

	public TexBlockEntity(BlockPos pos, BlockState state) {
		super(TestModule.TEX_TILE, pos, state, "0");
	}

	@Override
	public void load(CompoundTag compound) {
		readPacketData(compound);
		super.load(compound);
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag) {
		writePacketData(compoundTag);
		super.saveAdditional(compoundTag);
	}

}
