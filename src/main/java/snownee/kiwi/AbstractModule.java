package snownee.kiwi;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;

public abstract class AbstractModule
{
    protected void preInit()
    {
        // NO-OP
    }

    protected void init(FMLCommonSetupEvent event)
    {
        // NO-OP
    }

    protected void clientInit(FMLClientSetupEvent event)
    {
        // NO-OP
    }

    protected void serverInit(FMLDedicatedServerSetupEvent event)
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
