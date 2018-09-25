package snownee.kiwi.util;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

@Deprecated // Prototype?
public class RegistryUtil
{
    private RegistryUtil()
    {
    }

    public static void setItemProperties(Item item, Object... arguments)
    {
        for (Object o : arguments)
        {
            if (o instanceof CreativeTabs)
            {
                item.setCreativeTab((CreativeTabs) o);
            }
        }
    }

    public static void setBlockProperties(Block block, Object... arguments)
    {
        for (Object o : arguments)
        {
            if (o instanceof CreativeTabs)
            {
                block.setCreativeTab((CreativeTabs) o);
            }
            //            else if (o instanceof SoundType)
            //            {
            //                block.setSoundType();
            //            }
            //            else if (o instanceof MapColor) {
            //                block.blockMapColor
            //            }
        }
    }
}
