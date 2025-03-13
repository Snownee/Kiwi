package snownee.kiwi.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import snownee.kiwi.Kiwi;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class KiwiDataGen implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(KiwiLanguageProvider::new);
	}

	@SubscribeEvent
	public static void on(GatherDataEvent event) {
		//noinspection UnstableApiUsage
		FabricDataGenHelper.runDatagenForMod(Kiwi.ID, Kiwi.ID, new KiwiDataGen(), event);
	}
}