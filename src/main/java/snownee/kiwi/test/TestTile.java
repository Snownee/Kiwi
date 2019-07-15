package snownee.kiwi.test;

import snownee.kiwi.tile.TextureTile;

public class TestTile extends TextureTile
{

    public TestTile()
    {
        super(TestModule.FIRST_TILE, "top", "side", "bottom");
        persistData = true;
    }

}
