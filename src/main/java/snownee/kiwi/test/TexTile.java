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
