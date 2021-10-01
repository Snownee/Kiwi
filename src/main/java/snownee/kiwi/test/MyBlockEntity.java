package snownee.kiwi.test;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.block.entity.BaseBlockEntity;

public class MyBlockEntity extends BaseBlockEntity {

	public MyBlockEntity(BlockPos pos, BlockState state) {
		super(TestModule.FIRST_TILE, pos, state);
		persistData = true;
	}

	@Override
	protected void readPacketData(CompoundTag data) {
	}

	@Override
	protected CompoundTag writePacketData(CompoundTag data) {
		return data;
	}

	@Override
	public void load(CompoundTag data) {
		readPacketData(data);
		super.load(data);
	}

	@Override
	public CompoundTag save(CompoundTag data) {
		writePacketData(data);
		return super.save(data);
	}
}