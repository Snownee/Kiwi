package snownee.kiwi.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemModBlock extends ItemBlock
{

    public ItemModBlock(Block block)
    {
        super(block);
    }

    @Override
    public int getMetadata(int damage)
    {
        return hasSubtypes ? damage : 0;
    }

    @Override
    public String getTranslationKey(ItemStack stack)
    {
        if (hasSubtypes)
        {
            return super.getTranslationKey(stack) + "." + stack.getMetadata();
        }
        else
        {
            return super.getTranslationKey(stack);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        ItemMod.addTip(stack, tooltip);
    }

}
