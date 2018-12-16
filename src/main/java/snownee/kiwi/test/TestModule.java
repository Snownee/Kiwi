package snownee.kiwi.test;

import com.jcraft.jorbis.Block;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import snownee.kiwi.IModule;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.block.BlockMod;
import snownee.kiwi.potion.PotionMod;

@KiwiModule(modid = Kiwi.MODID, optional = true)
public class TestModule implements IModule
{
    // Register a simple item. Kiwi will automatically register and map models
    public static final ItemTest FIRST_ITEM = new ItemTest("my_first_item");

    public static final ItemVariantTest<Block, VariantTest> MISC = new ItemVariantTest<>("misc");

    // Register a simple block and its ItemBlock
    public static final BlockMod FIRST_BLOCK = new BlockMod("my_first_block", Material.ROCK);

    // Register a simple potion and its PotionEffect
    public static final PotionMod FIRST_POTION = new PotionMod("my_first_potion", false, 0, false, 0xFF0000, -1, true);

    @Override
    public void init()
    {
        FIRST_BLOCK.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        FIRST_ITEM.setCreativeTab(CreativeTabs.MISC);
        MISC.setCreativeTab(CreativeTabs.MISC);
    }
}
