package snownee.kiwi.block;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistryEntry;
import snownee.kiwi.client.ModelUtil;

public interface IModBlock extends IForgeRegistryEntry<Block>
{
    String getName();

    void register(String modid);

    Block cast();

    default boolean hasItem()
    {
        return true;
    }

    @SideOnly(Side.CLIENT)
    default void mapModel()
    {
        if (hasItem())
        {
            ModelUtil.mapItemModel(Item.getItemFromBlock(cast()));
        }
    }
}
