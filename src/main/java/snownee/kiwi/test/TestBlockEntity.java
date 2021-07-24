package snownee.kiwi.test;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.block.entity.TextureBlockEntity;

public class TestBlockEntity extends TextureBlockEntity {

	public TestBlockEntity(BlockPos pos, BlockState state) {
		super(TestModule.FIRST_TILE, pos, state, "top", "side", "bottom");
	}

	@Override
	public boolean isMark(String k) {
		return k.equals("top");
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
