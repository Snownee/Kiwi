package snownee.kiwi.test;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import snownee.kiwi.IModule;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.block.BlockMod;

@KiwiModule(modid = Kiwi.MODID, optional = true)
public class TestModule implements IModule
{
    // Register a simple item. Kiwi will automatically register and map models
    public static final ItemTest FIRST_ITEM = new ItemTest("my_first_item");

    // Register a simple block and its ItemBlock
    public static final BlockMod FIRST_BLOCK = new BlockMod("my_first_block", Material.ROCK);

    @Override
    public void init()
    {
        FIRST_ITEM.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        FIRST_ITEM.setCreativeTab(CreativeTabs.MISC);
    }
}
