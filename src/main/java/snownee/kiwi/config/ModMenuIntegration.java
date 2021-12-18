package snownee.kiwi.config;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import snownee.kiwi.Kiwi;
import snownee.kiwi.loader.Platform;

public class ModMenuIntegration implements ModMenuApi {

	private Map<String, ConfigScreenFactory<?>> cachedFactories;

	public Map<String, ConfigScreenFactory<?>> factories() {
		if (cachedFactories == null) {
			if (Platform.isModLoaded("cloth-config")) {
				Set<String> mods = KiwiConfigManager.allConfigs.stream().map($ -> $.getModId()).collect(Collectors.toSet());
				Map<String, ConfigScreenFactory<?>> factories = Maps.newHashMap();
				for (String mod : mods) {
					factories.put(mod, $ -> ClothConfigIntegration.create($, mod));
				}
				cachedFactories = ImmutableMap.copyOf(factories);
			} else {
				cachedFactories = ImmutableMap.of();
			}
		}
		return cachedFactories;
	}

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return factories().getOrDefault(Kiwi.MODID, screen -> null);
	}

	@Override
	public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
		return factories();
	}

}
