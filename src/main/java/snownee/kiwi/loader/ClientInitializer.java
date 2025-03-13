package snownee.kiwi.loader;

import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import snownee.kiwi.client.TooltipEvents;
import snownee.kiwi.command.ClientCommandContext;
import snownee.kiwi.command.KalcCommand;
import snownee.kiwi.command.KiwiClientCommand;

public class ClientInitializer {

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void globalTooltip(ItemTooltipEvent event) {
		TooltipEvents.globalTooltip(event.getItemStack(), event.getToolTip(), event.getFlags());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void debugTooltip(ItemTooltipEvent event) {
		TooltipEvents.debugTooltip(event.getItemStack(), event.getToolTip(), event.getFlags());
	}

	@SubscribeEvent
	public static void registerClientCommand(RegisterClientCommandsEvent event) {
		ClientCommandContext<CommandSourceStack> context = new ClientCommandContext<>(event.getBuildContext());
		event.getDispatcher().register(KiwiClientCommand.create(context));
		event.getDispatcher().register(KalcCommand.create(context));
	}

}
