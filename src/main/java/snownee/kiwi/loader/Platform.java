package snownee.kiwi.loader;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.fmllegacy.server.ServerLifecycleHooks;

public class Platform {

	public static boolean isModLoaded(String id) {
		return ModList.get().isLoaded(id);
	}

	public static boolean isPhysicalClient() {
		return FMLEnvironment.dist.isClient();
	}

	public static boolean isLogicalClient() {
		return EffectiveSide.get().isClient();
	}

	public static MinecraftServer getServer() {
		return ServerLifecycleHooks.getCurrentServer();
	}

	public static boolean isProduction() {
		return FMLEnvironment.production;
	}

}
