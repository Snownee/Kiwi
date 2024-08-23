package snownee.kiwi.config;

import java.util.List;
import java.util.Objects;

import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public class NeoClothConfigIntegration {
	public static void init() {
		List<String> mods = KiwiConfigManager.allConfigs.stream().map(ConfigHandler::getModId).distinct().toList();
		for (String mod : mods) {
			List<ConfigHandler> configs = KiwiConfigManager.getModHandlersWithScreen(mod, ClothConfigIntegration.attributes());
			if (configs.isEmpty()) {
				continue;
			}
			ModList.get().getModContainerById(mod).ifPresent($ -> $.registerExtensionPoint(
					IConfigScreenFactory.class,
					(container, screen) -> Objects.requireNonNull(ClothConfigIntegration.create(screen, container.getModId()))));
		}
	}
}
