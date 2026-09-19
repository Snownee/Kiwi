package snownee.kiwi.config;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.resources.Identifier;
import snownee.kiwi.config.ConfigHandler.Value;
import snownee.kiwi.config.KiwiConfig.ConfigType;

public class KiwiConfigManager {

	public static final List<ConfigHandler> allConfigs = Lists.newLinkedList();
	private static final Map<Class<?>, ConfigHandler> clazz2Configs = Maps.newHashMap();
	public static final Map<Identifier, Value<Boolean>> modules = Maps.newHashMap();

	public static synchronized void register(ConfigHandler configHandler) {
		allConfigs.add(configHandler);
		Class<?> clazz = configHandler.getClazz();
		if (clazz != null) {
			clazz2Configs.put(clazz, configHandler);
		}
	}

	public static void init(Map<Identifier, Boolean> moduleOptions) {
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
			config.init(moduleOptions);
		}
		for (Identifier rl : moduleOptions.keySet()) {
			if (settledMods.contains(rl.getNamespace())) {
				continue;
			}
			settledMods.add(rl.getNamespace());
			ConfigHandler config = new ConfigHandler(rl.getNamespace(), rl.getNamespace() + "-modules", ConfigType.COMMON, null, true);
			config.init(moduleOptions);
		}
	}

	public static void defineModules(String modId, ConfigHandler builder, Map<Identifier, Boolean> moduleOptions, boolean subcategory) {
		String prefix = subcategory ? "modules." : "";
		for (Entry<Identifier, Boolean> entry : moduleOptions.entrySet()) {
			Identifier rl = entry.getKey();
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

	/**
	 * Resolves a value by a combined path of "{fileName}.{valuePath}", where the file name has no
	 * extension and any '-' in it may be written as '.'.
	 *
	 * @param path the combined path
	 * @return the matching value, or {@code null} if no config or value matches
	 */
	public static ConfigHandler.@Nullable Value<?> getValue(String path) {
		ConfigHandler bestHandler = null;
		String bestPath = null;
		int bestScore = -1;
		for (ConfigHandler config : allConfigs) {
			String fileName = config.getFileName();
			String rest = null;
			if (path.startsWith(fileName + ".")) {
				rest = path.substring(fileName.length() + 1);
			} else {
				String normalized = fileName.replace('-', '.') + ".";
				if (path.startsWith(normalized)) {
					rest = path.substring(normalized.length());
				}
			}
			if (rest != null && !rest.isEmpty() && fileName.length() > bestScore) {
				bestHandler = config;
				bestPath = rest;
				bestScore = fileName.length();
			}
		}
		return bestHandler == null ? null : bestHandler.get(bestPath);
	}

	public static List<String> getModsWithScreen(ConfigLibAttributes attributes) {
		return allConfigs.stream().filter(c -> c.providesConfigScreen(attributes)).map(ConfigHandler::getModId).distinct().toList();
	}

	public static List<ConfigHandler> getModHandlersWithScreen(String modId, ConfigLibAttributes attributes) {
		return allConfigs.stream().filter(c -> c.getModId().equals(modId) && c.providesConfigScreen(attributes)).toList();
	}

}
