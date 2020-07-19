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
