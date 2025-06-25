package snownee.kiwi.test;

import net.minecraft.world.level.block.Blocks;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.loader.event.InitEvent;
import snownee.kiwi.loader.event.PostInitEvent;

public class MyModule extends AbstractModule {

	@Override
	protected void init(InitEvent event) {
		event.enqueueWork(() -> Platform.registerAxeConversion(Blocks.DIAMOND_BLOCK, Blocks.REDSTONE_BLOCK));
	}

}
