package snownee.kiwi.item;

import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistryEntry;
import snownee.kiwi.client.ModelUtil;

public interface IModItem extends IForgeRegistryEntry<Item>
{
    String getName();

    void register(String modid);

    Item cast();

    @SideOnly(Side.CLIENT)
    default void mapModel()
    {
        ModelUtil.mapItemModel(cast());
    }
}
