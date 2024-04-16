package snownee.kiwi.loader;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.resources.ResourceLocation;
import snownee.kiwi.Kiwi;
import snownee.kiwi.client.TooltipEvents;

public class ClientInitializer implements ClientModInitializer {

	public static final ResourceLocation HIGH = new ResourceLocation(Kiwi.ID, "high");
	public static final ResourceLocation LOW = new ResourceLocation(Kiwi.ID, "low");

	@Override
	public void onInitializeClient() {
		ItemTooltipCallback.EVENT.addPhaseOrdering(HIGH, Event.DEFAULT_PHASE);
		ItemTooltipCallback.EVENT.addPhaseOrdering(Event.DEFAULT_PHASE, LOW);
		ItemTooltipCallback.EVENT.register(HIGH, (stack, context, type, lines) -> TooltipEvents.globalTooltip(stack, lines, type));
		ItemTooltipCallback.EVENT.register(LOW, (stack, context, type, lines) -> TooltipEvents.debugTooltip(stack, lines, type));
	}

}
