package snownee.kiwi.test;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.block.entity.RetextureBlockEntity;

public class TestBlockEntity extends RetextureBlockEntity {

	public TestBlockEntity(BlockPos pos, BlockState state) {
		super(TestModule.FIRST_TILE, pos, state, "0");
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
