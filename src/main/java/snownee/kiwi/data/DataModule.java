package snownee.kiwi.data;

import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.data.loot.AddLootTable;

@KiwiModule(name = "data")
public class DataModule extends AbstractModule {

    public static final AddLootTable.Serializer ADD_LOOT = new AddLootTable.Serializer();

}
