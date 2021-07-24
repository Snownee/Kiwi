package snownee.kiwi.test;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.block.entity.TextureBlockEntity;

public class TexBlockEntity extends TextureBlockEntity {

	public TexBlockEntity(BlockPos pos, BlockState state) {
		super(TestModule.TEX_TILE, pos, state, "wool");
	}

	@Override
	public boolean isMark(String k) {
		return k.equals("wool");
	}

	@Override
	public void load(CompoundTag compound) {
		readPacketData(compound);
		super.load(compound);
	}

	@Override
	public CompoundTag save(CompoundTag compound) {
		writePacketData(compound);
		return super.save(compound);
	}

}
