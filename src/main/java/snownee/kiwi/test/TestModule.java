package snownee.kiwi.test;

import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiManager;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.block.ModBlock;

@KiwiModule(modid = Kiwi.MODID, name = "test")
@KiwiModule.Optional(disabledByDefault = true)
@KiwiModule.Group("buildingBlocks")
public class TestModule extends AbstractModule
{
    // Register a simple item. Kiwi will automatically register and map models
    public static final TestItem FIRST_ITEM = new TestItem(itemProp().rarity(Rarity.EPIC));

    public static final Item.Properties FIRST_BLOCK_ITEM_BUILDER = itemProp().rarity(Rarity.RARE);
    // Register a simple block and its ItemBlock
    public static final ModBlock FIRST_BLOCK = new ModBlock(blockProp(Material.WOOD));

    //    // Register a simple potion and its PotionEffect
    //    public static final PotionMod FIRST_POTION = new PotionMod("my_first_potion", false, 0, false, 0xFF0000, -1, true);

    public static final ItemGroup GROUP = new ItemGroup("test")
    {
        @Override
        @OnlyIn(Dist.CLIENT)
        public ItemStack createIcon()
        {
            return new ItemStack(FIRST_ITEM);
        }
    };

    //    @Override
    //    public void init()
    //    {
    //        FIRST_BLOCK.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    //        FIRST_ITEM.setCreativeTab(CreativeTabs.MISC);
    //        MISC.setCreativeTab(CreativeTabs.MISC);
    //    }

}
