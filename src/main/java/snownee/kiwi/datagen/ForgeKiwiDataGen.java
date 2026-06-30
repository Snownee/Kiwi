package snownee.kiwi.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.loader.api.FabricLoader;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import snownee.kiwi.Kiwi;

@EventBusSubscriber
public class ForgeKiwiDataGen {
	@SubscribeEvent
	public static void gather(GatherDataEvent.Client event) {
		var modContainer = FabricLoader.getInstance().getModContainer(Kiwi.ID).orElseThrow();
		var output = new FabricPackOutput(
				modContainer,
				event.getGenerator().getPackOutput().getOutputFolder(),
				event.validate());
		var registries = event.getLookupProvider();
		event.addProvider(new KiwiLanguageProvider(output, registries));
	}
}