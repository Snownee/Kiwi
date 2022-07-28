package snownee.kiwi.loader;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import snownee.kiwi.Kiwi;

public class Platform {

	private Platform() {
	}

	public static boolean isModLoaded(String id) {
		return FabricLoader.getInstance().isModLoaded(id);
	}

	public static boolean isPhysicalClient() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
	}

	public static MinecraftServer getServer() {
		return Kiwi.currentServer;
	}

	public static boolean isProduction() {
		return !FabricLoader.getInstance().isDevelopmentEnvironment();
	}

	public static boolean isDataGen() {
		return FabricDataGenHelper.ENABLED;
	}

}
