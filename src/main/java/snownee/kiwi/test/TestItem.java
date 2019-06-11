package snownee.kiwi.test;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
//import snownee.kiwi.client.AdvancedFontRenderer;
import snownee.kiwi.item.ModItem;

// Your class don't have to extends ModItem or ModBlock to be registered
public class TestItem extends ModItem
{

    public TestItem(Item.Properties builder)
    {
        super(builder);
    }

//    @Override
//    public FontRenderer getFontRenderer(ItemStack stack)
//    {
//        return AdvancedFontRenderer.INSTANCE;
//    }

//    @Override
//    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn)
//    {
//        ItemStack stack = playerIn.getHeldItem(handIn);
//        NBTTagCompound tag = NBTHelper.of(stack).setInt("Fluid.Amount", 1000).getTag("Fluid");
//        System.out.println(tag);
//        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
//    }
}
