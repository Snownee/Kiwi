package snownee.kiwi.customization;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.IModInfo;

public final class CustomizationServiceFinder {
	public static final Path PACK_DIRECTORY = FMLPaths.GAMEDIR.get().resolve("kiwipacks");

	public static boolean shouldEnable(List<? extends IModInfo> mods) {
		File[] files = PACK_DIRECTORY.toFile().listFiles(file -> file.isDirectory() || file.getName().endsWith(".zip"));
		if (files != null && files.length > 0) {
			return true;
		}
		for (IModInfo mod : mods) {
			if (mod.getModProperties().containsKey("kiwiCustomization")) {
				return true;
			}
		}
		return false;
	}
}
