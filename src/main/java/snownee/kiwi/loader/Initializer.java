package snownee.kiwi.loader;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import snownee.kiwi.client.TooltipEvents;

@EventBusSubscriber
public class Initializer {

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void globalTooltip(ItemTooltipEvent event) {
		TooltipEvents.globalTooltip(event.getItemStack(), event.getToolTip(), event.getFlags());
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void debugTooltip(ItemTooltipEvent event) {
		TooltipEvents.debugTooltip(event.getItemStack(), event.getToolTip(), event.getFlags());
	}

}
