package snownee.kiwi.loader;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.resources.ResourceLocation;
import snownee.kiwi.Kiwi;
import snownee.kiwi.client.TooltipEvents;

public class Initializer implements ClientModInitializer {

	public static final ResourceLocation HIGH = Kiwi.id("high");
	public static final ResourceLocation LOW = Kiwi.id("low");

	@Override
	public void onInitializeClient() {
		ItemTooltipCallback.EVENT.addPhaseOrdering(HIGH, Event.DEFAULT_PHASE);
		ItemTooltipCallback.EVENT.addPhaseOrdering(Event.DEFAULT_PHASE, LOW);
		ItemTooltipCallback.EVENT.register(HIGH, (stack, context, lines) -> TooltipEvents.globalTooltip(stack, lines, context));
		ItemTooltipCallback.EVENT.register(LOW, (stack, context, lines) -> TooltipEvents.debugTooltip(stack, lines, context));
	}

}
