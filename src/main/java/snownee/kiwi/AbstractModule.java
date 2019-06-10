package snownee.kiwi;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

public abstract class AbstractModule
{
    protected void preInit()
    {
        // NO-OP
    }

    protected void init()
    {
        // NO-OP
    }

    protected void postInit()
    {
        // NO-OP
    }

    protected static Item.Properties itemProp()
    {
        return new Item.Properties();
    }

    protected static Block.Properties blockProp(Material material)
    {
        return Block.Properties.create(material);
    }
}
