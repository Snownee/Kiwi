package snownee.kiwi.config;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.resources.ResourceLocation;
import snownee.kiwi.Kiwi;
import snownee.kiwi.config.ConfigHandler.Value;
import snownee.kiwi.config.KiwiConfig.ConfigType;

public class KiwiConfigManager {

	public static final List<ConfigHandler> allConfigs = Lists.newLinkedList();
	private static final Map<Class<?>, ConfigHandler> clazz2Configs = Maps.newHashMap();
	public static final Map<ResourceLocation, Value<Boolean>> modules = Maps.newHashMap();

	public static synchronized void register(ConfigHandler configHandler) {
		allConfigs.add(configHandler);
		clazz2Configs.put(configHandler.getClazz(), configHandler);
	}

	public static void init() {
		allConfigs.sort(Comparator.comparing(ConfigHandler::getFileName));
		Set<String> settledMods = Sets.newHashSet();
		for (ConfigHandler config : allConfigs) {
			if (config.hasModules()) {
				settledMods.add(config.getModId());
			}
		}
		for (ConfigHandler config : allConfigs) {
			//			if (!config.hasModules() && config.getType() == ConfigType.COMMON && !settledMods.contains(config.getModId())) {
			//				settledMods.add(config.getModId());
			//				config.setHasModules(true);
			//			}
			config.init();
		}
		for (ResourceLocation rl : Kiwi.defaultOptions.keySet()) {
			if (settledMods.contains(rl.getNamespace())) {
				continue;
			}
			settledMods.add(rl.getNamespace());
			ConfigHandler config = new ConfigHandler(rl.getNamespace(), rl.getNamespace() + "-modules", ConfigType.COMMON, null, true);
			config.init();
		}
	}

	public static void defineModules(String modId, ConfigHandler builder, boolean subcategory) {
		String prefix = subcategory ? "modules." : "";
		for (Entry<ResourceLocation, Boolean> entry : Kiwi.defaultOptions.entrySet()) {
			ResourceLocation rl = entry.getKey();
			if (rl.getNamespace().equals(modId)) {
				Value<Boolean> value = builder.define(
						prefix + rl.getPath(),
						entry.getValue(),
						null,
						"%s.config.modules.%s".formatted(modId, rl.getPath()));
				value.requiresRestart = true;
				modules.put(rl, value);
			}
		}
	}

	public static void refresh() {
		allConfigs.forEach(ConfigHandler::refresh);
	}

	public static boolean refresh(String fileName) {
		if (fileName.endsWith(ConfigHandler.FILE_EXTENSION)) {
			fileName = fileName.substring(0, fileName.length() - ConfigHandler.FILE_EXTENSION.length());
		}
		for (ConfigHandler config : allConfigs) {
			if (config.getFileName().equals(fileName)) {
				config.refresh();
				return true;
			}
		}
		return false;
	}

	public static ConfigHandler getHandler(Class<?> clazz) {
		return clazz2Configs.get(clazz);
	}

	public static List<String> getModsWithScreen(ConfigLibAttributes attributes) {
		return allConfigs.stream().filter(c -> c.providesConfigScreen(attributes)).map(ConfigHandler::getModId).distinct().toList();
	}

	public static List<ConfigHandler> getModHandlersWithScreen(String modId, ConfigLibAttributes attributes) {
		return allConfigs.stream().filter(c -> c.getModId().equals(modId) && c.providesConfigScreen(attributes)).toList();
	}

}
