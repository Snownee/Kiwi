package snownee.kiwi.datagen;

import net.neoforged.neoforge.data.event.GatherDataEvent;
import snownee.kiwi.Kiwi;

public class KiwiDataGen {
	public static void on(GatherDataEvent event) {
		event.getGenerator().addProvider(
				true,
				new KiwiLanguageProvider(event.getGenerator().getPackOutput(), Kiwi.ID, event.getLookupProvider()));
	}
}