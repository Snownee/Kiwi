package snownee.kiwi.item;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import snownee.kiwi.block.IModBlock;

public class ItemModBlock extends ItemBlock implements IModItem
{
    protected final IModBlock iModBlock;

    public ItemModBlock(IModBlock block)
    {
        super(block.cast());
        this.iModBlock = block;
        if (block.getItemSubtypeAmount() > 1)
        {
            setHasSubtypes(true);
        }
    }

    @Override
    public String getName()
    {
        return iModBlock.getName();
    }

    @Override
    public void register(String modid)
    {
        setRegistryName(modid, getName());
        setTranslationKey(modid + "." + getName());
    }

    @Override
    public Item cast()
    {
        return this;
    }

    @Override
    public void mapModel()
    {
        iModBlock.mapModel();
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
