package snownee.kiwi.test;

import snownee.kiwi.datagen.provider.KiwiBlockLoot;

public class TestBlockLoot extends KiwiBlockLoot {

	public TestBlockLoot() {
		super(TestModule.INSTANCE.uid);
	}

	@Override
	protected void _addTables() {
		handleDefault($ -> noDrop());
		handle(TestBlock2.class, $ -> createGrassDrops($));
	}

}
