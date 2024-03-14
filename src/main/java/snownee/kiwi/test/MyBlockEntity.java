package snownee.kiwi.test;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.block.entity.ModBlockEntity;

public class MyBlockEntity extends ModBlockEntity {

	public MyBlockEntity(BlockPos pos, BlockState state) {
		super(TestModule.FIRST_TILE.get(), pos, state);
		persistData = true;
	}

	@Override
	protected void readPacketData(CompoundTag data) {
	}

	@Override
	protected CompoundTag writePacketData(CompoundTag data, HolderLookup.Provider provider) {
		return data;
	}

	@Override
	public void load(CompoundTag data, HolderLookup.Provider provider) {
		readPacketData(data);
		super.load(data, provider);
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		writePacketData(compoundTag, provider);
		super.saveAdditional(compoundTag, provider);
	}
}