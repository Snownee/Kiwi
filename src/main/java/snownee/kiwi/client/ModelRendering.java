package snownee.kiwi.client;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiManager;
import snownee.kiwi.block.IModBlock;
import snownee.kiwi.item.IModItem;

@EventBusSubscriber(modid = Kiwi.MODID, value = Side.CLIENT)
public class ModelRendering
{
    @SubscribeEvent
    public static void onModelRegister(ModelRegistryEvent event)
    {
        KiwiManager.BLOCKS.keySet().forEach(IModBlock::mapModel);
        KiwiManager.ITEMS.keySet().forEach(IModItem::mapModel);
    }
}
