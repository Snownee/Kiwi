package snownee.kiwi.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import snownee.kiwi.Kiwi;
import snownee.kiwi.config.KiwiConfig.ConfigType;

public class KiwiConfigManager {

	private static final List<ConfigHandler> allConfigs = Lists.newLinkedList();
	private static final Map<Class<?>, ConfigHandler> clazz2Configs = Maps.newHashMap();
	public static final Map<ResourceLocation, BooleanValue> modules = Maps.newHashMap();

	public static synchronized void register(ConfigHandler configHandler) {
		allConfigs.add(configHandler);
		if (configHandler.getClass() != null) {
			clazz2Configs.put(configHandler.getClazz(), configHandler);
		}
	}

	public static void init() {
		Collections.sort(allConfigs, (a, b) -> a.getFileName().compareTo(b.getFileName()));
		Set<String> settledMods = Sets.newHashSet();
		for (ConfigHandler config : allConfigs) {
			if (config.isMaster()) {
				settledMods.add(config.getModId());
			}
		}
		for (ConfigHandler config : allConfigs) {
			if (!config.isMaster() && config.getType() == ConfigType.COMMON && !settledMods.contains(config.getModId())) {
				settledMods.add(config.getModId());
				config.setMaster(true);
			}
			config.init();
		}
		for (ResourceLocation rl : Kiwi.defaultOptions.keySet()) {
			if (settledMods.contains(rl.getNamespace())) {
				continue;
			}
			settledMods.add(rl.getNamespace());
			ConfigHandler configHandler = new ConfigHandler(rl.getNamespace(), rl.getNamespace() + "-modules.toml", ConfigType.COMMON, null, true);
			configHandler.init();
		}
	}

	public static void preload() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		for (ConfigHandler configHandler : allConfigs) {
			if (configHandler.isMaster()) {
				configHandler.forceLoad();
			}
		}
	}

	public static void defineModules(String modId, Builder builder) {
		builder.push("modules");
		for (Entry<ResourceLocation, Boolean> entry : Kiwi.defaultOptions.entrySet()) {
			ResourceLocation rl = entry.getKey();
			if (rl.getNamespace().equals(modId)) {
				//				String translation = Util.makeDescriptionId("kiwi.config.module", rl);
				//				builder.translation(translation);
				modules.put(rl, builder.define(rl.getPath(), entry.getValue().booleanValue()));
			}
		}
		builder.pop();
	}

	public static void refresh() {
		allConfigs.forEach(ConfigHandler::refresh);
	}

	public static ConfigHandler getHandler(Class<?> clazz) {
		return clazz2Configs.get(clazz);
	}

}
