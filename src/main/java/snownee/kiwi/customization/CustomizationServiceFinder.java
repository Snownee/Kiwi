package snownee.kiwi.customization;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public final class CustomizationServiceFinder {
	public static final Path PACK_DIRECTORY = FabricLoader.getInstance().getGameDir().resolve("kiwipacks");

	public static boolean shouldEnable(Collection<ModContainer> mods) {
		File[] files = PACK_DIRECTORY.toFile().listFiles(file -> file.isDirectory() || file.getName().endsWith(".zip"));
		if (files != null && files.length > 0) {
			return true;
		}
		for (ModContainer mod : mods) {
			if (mod.getMetadata().containsCustomValue("kiwiCustomization")) {
				return true;
			}
		}
		return false;
	}
}
