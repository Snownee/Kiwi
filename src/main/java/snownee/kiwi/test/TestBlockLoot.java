package snownee.kiwi.test;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import snownee.kiwi.datagen.KiwiBlockLoot;

public class TestBlockLoot extends KiwiBlockLoot {

	public TestBlockLoot(FabricDataOutput dataOutput) {
		super(TestModule.INSTANCE.uid, dataOutput);
	}

	@Override
	protected void addTables() {
		handleDefault($ -> noDrop());
		handle(TestBlock2.class, $ -> createGrassDrops($));
	}

}
