package snownee.kiwi.config;

import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.Maps;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import snownee.kiwi.Kiwi;
import snownee.kiwi.loader.Platform;

public class ModMenuIntegration implements ModMenuApi {

	private @Nullable Map<String, ConfigScreenFactory<?>> cachedFactories;

	public Map<String, ConfigScreenFactory<?>> factories() {
		if (cachedFactories == null) {
			Kiwi.onInitialize();
			if (Platform.isModLoaded("cloth-config")) {
				List<String> mods = KiwiConfigManager.getModsWithScreen(ClothConfigIntegration.attributes());
				Map<String, ConfigScreenFactory<?>> factories = Maps.newHashMap();
				for (String mod : mods) {
					factories.put(mod, $ -> ClothConfigIntegration.create($, mod));
				}
				cachedFactories = Map.copyOf(factories);
			} else {
				cachedFactories = Map.of();
			}
		}
		return cachedFactories;
	}

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return factories().getOrDefault(Kiwi.ID, _ -> null);
	}

	@Override
	public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
		return factories();
	}

}
