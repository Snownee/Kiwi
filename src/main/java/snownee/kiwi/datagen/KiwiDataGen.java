package snownee.kiwi.datagen;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import snownee.kiwi.Kiwi;

@EventBusSubscriber(modid = Kiwi.ID, bus = EventBusSubscriber.Bus.MOD)
public class KiwiDataGen {
	@SubscribeEvent
	public static void on(GatherDataEvent event) {
		if (!event.getMods().contains(Kiwi.ID)) {
			return;
		}
		event.getGenerator().addProvider(
				event.includeClient(),
				new KiwiLanguageProvider(event.getGenerator().getPackOutput(), Kiwi.ID, event.getLookupProvider()));
	}
}