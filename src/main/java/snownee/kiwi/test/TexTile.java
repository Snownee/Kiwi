package snownee.kiwi.test;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import snownee.kiwi.tile.TextureTile;

public class TexTile extends TextureTile {

	public TexTile() {
		super(TestModule.TEX_TILE, "wool");
	}

	@Override
	public boolean isMark(String k) {
		return k.equals("wool");
	}

	@Override
	public void read(BlockState state, CompoundNBT compound) {
		readPacketData(compound);
		super.read(state, compound);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		writePacketData(compound);
		return super.write(compound);
	}

}
