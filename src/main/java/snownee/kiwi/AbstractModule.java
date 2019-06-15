package snownee.kiwi;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import snownee.kiwi.block.ModBlock;

/**
 * 
 * All your modules should extend {@code AbstractModule}
 * 
 * @author Snownee
 *
 */
public abstract class AbstractModule
{
    protected void preInit()
    {
        // NO-OP
    }

    /**
     * @author Snownee
     * @param event Note: this event's ModContainer is from Kiwi
     */
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

    /// helper methods:
    protected static Item.Properties itemProp()
    {
        return new Item.Properties();
    }

    protected static Block.Properties blockProp(Material material)
    {
        return Block.Properties.create(material);
    }

    protected static <T extends Block> T init(T block)
    {
        return ModBlock.deduceSoundAndHardness(block);
    }
}
