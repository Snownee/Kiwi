package snownee.kiwi.test;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import snownee.kiwi.tile.TextureTile;

public class TestTile extends TextureTile {

	public TestTile() {
		super(TestModule.FIRST_TILE, "top", "side", "bottom");
	}

	@Override
	public boolean isMark(String k) {
		return k.equals("top");
	}

	@Override
	public void load(BlockState state, CompoundNBT compound) {
		readPacketData(compound);
		super.load(state, compound);
	}

	@Override
	public CompoundNBT save(CompoundNBT compound) {
		writePacketData(compound);
		return super.save(compound);
	}

}
