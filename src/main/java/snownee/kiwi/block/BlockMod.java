package snownee.kiwi.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockMod extends Block implements IModBlock
{
    private final String name;

    public BlockMod(String name, Material materialIn)
    {
        super(materialIn);
        this.name = name;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void mapModel()
    {
        if (hasItem())
        {
            for (int i = 0; i < getItemSubtypeAmount(); i++)
            {
                ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), i, new ModelResourceLocation(getRegistryName(), "inventory"));
            }
        }
    }

    @Override
    public void register(String modid)
    {
        setRegistryName(modid, getName());
        setTranslationKey(modid + "." + getName());
    }

    @Override
    public Block cast()
    {
        return this;
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items)
    {
        for (int i = 0; i < getItemSubtypeAmount(); i++)
        {
            items.add(new ItemStack(this, 1, i));
        }
    }
}
