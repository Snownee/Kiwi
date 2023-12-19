package snownee.kiwi.loader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.server.MinecraftServer;
import snownee.kiwi.Kiwi;

public class Platform {

	private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+).*?$");

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

	public static int getVersionNumber(String id) {
		ModContainer container = FabricLoader.getInstance().getModContainer(id).orElseThrow();
		String version = container.getMetadata().getVersion().getFriendlyString();
		Matcher matcher = VERSION_PATTERN.matcher(version);
		int result = 0;
		if (!matcher.matches()) {
			throw new RuntimeException("Invalid version string: " + version);
		}
		for (int i = 1; i <= 3; i++) {
			int group = Math.min(Integer.parseInt(matcher.group(i)), 99);
			result = result * 100 + group;
		}
		return result;
	}

	public static Platform.Type getPlatform() {
		return Type.Fabric;
	}

	public static Platform.Type getPlatformSeries() {
		return Type.Fabric;
	}

	public enum Type {
		Vanilla, Fabric, Quilt, Forge, NeoForge
	}

}
