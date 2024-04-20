package snownee.kiwi.test;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.block.entity.RetextureBlockEntity;

public class TexBlockEntity extends RetextureBlockEntity {

	public TexBlockEntity(BlockPos pos, BlockState state) {
		super(TestModule.TEX_TILE.get(), pos, state, "0");
	}

	@Override
	protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		readPacketData(compoundTag);
		super.loadAdditional(compoundTag, provider);
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		writePacketData(compoundTag, provider);
		super.saveAdditional(compoundTag, provider);
	}

}
