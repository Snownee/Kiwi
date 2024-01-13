package snownee.kiwi.test;

import net.minecraft.world.level.block.Blocks;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.loader.event.InitEvent;
import snownee.kiwi.loader.event.PostInitEvent;
import snownee.kiwi.util.VanillaActions;

public class MyModule extends AbstractModule {

	@Override
	protected void preInit() {
	}

	@Override
	protected void init(InitEvent event) {
		event.enqueueWork(() -> VanillaActions.registerAxeConversion(Blocks.DIAMOND_BLOCK, Blocks.REDSTONE_BLOCK));
	}

	@Override
	protected void postInit(PostInitEvent event) {
	}

}
