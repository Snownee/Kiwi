package snownee.kiwi.datagen;

import net.neoforged.neoforge.data.event.GatherDataEvent;
import snownee.kiwi.Kiwi;

public class KiwiDataGen {
	public static void on(GatherDataEvent.Client event) {
		event.getGenerator().addProvider(
				true,
				new KiwiLanguageProvider(event.getGenerator().getPackOutput(), Kiwi.ID, event.getLookupProvider()));
	}
}