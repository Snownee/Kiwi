package snownee.kiwi.test;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.block.Blocks;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.loader.event.ClientInitEvent;
import snownee.kiwi.loader.event.InitEvent;
import snownee.kiwi.loader.event.PostInitEvent;
import snownee.kiwi.loader.event.ServerInitEvent;
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
	@Environment(EnvType.CLIENT)
	protected void clientInit(ClientInitEvent event) {
	}

	@Override
	protected void serverInit(ServerInitEvent event) {
	}

	@Override
	protected void postInit(PostInitEvent event) {
	}

	//	@Override
	//	protected void gatherData(GatherDataEvent event) {
	//	}

}
