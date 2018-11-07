package snownee.kiwi.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
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
        this(name, materialIn, deduceSoundType(materialIn));
    }

    public BlockMod(String name, Material materialIn, SoundType soundType)
    {
        super(materialIn);
        this.name = name;
        setSoundType(soundType);
        setHardness(deduceHardness(materialIn));
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
        for (int i = 0; i < getItemSubtypeAmount(); i++)
        {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), i, new ModelResourceLocation(getRegistryName(), "inventory"));
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

    public static SoundType deduceSoundType(final Material material)
    {
        if (material == Material.WOOD || material == Material.GOURD)
        {
            return SoundType.WOOD;
        }
        if (material == Material.GROUND || material == Material.CLAY)
        {
            return SoundType.GROUND;
        }
        if (material == Material.PLANTS || material == Material.GRASS || material == Material.LEAVES || material == Material.VINE || material == Material.SPONGE || material == Material.TNT)
        {
            return SoundType.PLANT;
        }
        if (material == Material.IRON)
        {
            return SoundType.METAL;
        }
        if (material == Material.GLASS || material == Material.PORTAL || material == Material.ICE || material == Material.PACKED_ICE || material == Material.REDSTONE_LIGHT)
        {
            return SoundType.GLASS;
        }
        if (material == Material.CLOTH || material == Material.CARPET || material == Material.CACTUS || material == Material.CAKE || material == Material.FIRE)
        {
            return SoundType.CLOTH;
        }
        if (material == Material.SAND)
        {
            return SoundType.SAND;
        }
        if (material == Material.SNOW || material == Material.CRAFTED_SNOW)
        {
            return SoundType.SNOW;
        }
        if (material == Material.ANVIL)
        {
            return SoundType.ANVIL;
        }
        return SoundType.STONE;
    }

    public static float deduceHardness(final Material material)
    {
        if (material == Material.PLANTS || material == Material.CIRCUITS || material == Material.AIR)
        {
            return 0;
        }
        if (material == Material.ROCK)
        {
            return 2.5F;
        }
        if (material == Material.WOOD)
        {
            return 2;
        }
        if (material == Material.GRASS)
        {
            return 0.6F;
        }
        if (material == Material.SAND || material == Material.GROUND || material == Material.CLAY)
        {
            return 0.5F;
        }
        if (material == Material.GLASS)
        {
            return 0.3F;
        }
        if (material == Material.IRON || material == Material.ANVIL)
        {
            return 5;
        }
        if (material == Material.WEB)
        {
            return 4;
        }
        if (material == Material.CLOTH)
        {
            return 0.8F;
        }
        if (material == Material.WATER || material == Material.LAVA)
        {
            return 100;
        }
        return 1;
    }
}
