package snownee.kiwi.test;

import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
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
		event.enqueueWork(() -> VanillaActions.registerCompostable(0.5F, Items.DIAMOND));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected void clientInit(ClientInitEvent event) {
	}

	@Override
	protected void serverInit(ServerInitEvent event) {
	}

	@Override
	protected void postInit(PostInitEvent event) {
	}

	@Override
	protected void gatherData(GatherDataEvent event) {
	}

}
