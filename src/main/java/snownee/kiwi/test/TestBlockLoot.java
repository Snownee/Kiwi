package snownee.kiwi.test;

import java.util.Set;

import net.minecraft.world.flag.FeatureFlags;
import snownee.kiwi.datagen.provider.KiwiBlockLoot;

public class TestBlockLoot extends KiwiBlockLoot {

	public TestBlockLoot() {
		super(TestModule.INSTANCE.uid, Set.of(), FeatureFlags.REGISTRY.allFlags());
	}

	@Override
	protected void addTables() {
		handleDefault($ -> noDrop());
		handle(TestBlock2.class, $ -> createGrassDrops($));
	}

}
