package snownee.kiwi.loader;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import snownee.kiwi.client.TooltipEvents;

public class ClientInitializer {

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void globalTooltip(ItemTooltipEvent event) {
		TooltipEvents.globalTooltip(event.getItemStack(), event.getToolTip(), event.getFlags());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void debugTooltip(ItemTooltipEvent event) {
		TooltipEvents.debugTooltip(event.getItemStack(), event.getToolTip(), event.getFlags());
	}

}
