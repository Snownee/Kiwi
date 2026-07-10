package snownee.kiwi.loader;

import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import snownee.kiwi.client.TooltipEvents;
import snownee.kiwi.command.ClientCommandContext;
import snownee.kiwi.command.KalcCommand;
import snownee.kiwi.command.KiwiClientCommand;
import snownee.kiwi.minieffects.EffectRenderingScreen;
import snownee.kiwi.minieffects.MiniEffects;

public class ClientInitializer {

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void globalTooltip(ItemTooltipEvent event) {
		TooltipEvents.globalTooltip(event.getItemStack(), event.getToolTip(), event.getFlags());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void debugTooltip(ItemTooltipEvent event) {
		TooltipEvents.debugTooltip(event.getItemStack(), event.getContext(), event.getFlags(), event.getToolTip());
	}

	@SubscribeEvent
	public static void registerClientCommand(RegisterClientCommandsEvent event) {
		ClientCommandContext<CommandSourceStack> context = new ClientCommandContext<>(event.getBuildContext());
		event.getDispatcher().register(KiwiClientCommand.create(context));
		event.getDispatcher().register(KalcCommand.create(context));
	}

	@SubscribeEvent
	public static void onMouseButtonPre(ScreenEvent.MouseButtonPressed.Pre event) {
		if (event.getScreen() instanceof EffectRenderingScreen screen) {
			if (!MiniEffects.allowClick(screen, event.getMouseX(), event.getMouseY())) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void onMouseButtonPost(ScreenEvent.MouseButtonPressed.Post event) {
		if (event.getScreen() instanceof EffectRenderingScreen screen) {
			MiniEffects.afterClick(screen, event.getMouseX(), event.getMouseY());
		}
	}

}
