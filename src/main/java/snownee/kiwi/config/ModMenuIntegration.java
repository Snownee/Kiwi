package snownee.kiwi.config;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiClientConfig;

public class ModMenuIntegration implements ModMenuApi {

	private static @Nullable Map<String, ConfigScreenFactory<?>> cachedFactories;
	private static @Nullable ConfigUiPreference cachedPreference;

	public Map<String, ConfigScreenFactory<?>> factories() {
		Kiwi.onInitialize();
		ConfigUiPreference preference = KiwiClientConfig.configUiPreference;
		if (cachedFactories == null || cachedPreference != preference) {
			List<ConfigLibAttributes> attributes = ConfigScreens.attributes();
			cachedPreference = preference;
			if (attributes.isEmpty()) {
				cachedFactories = Map.of();
			} else {
				Set<String> mods = Sets.newLinkedHashSet();
				for (ConfigLibAttributes attribute : attributes) {
					mods.addAll(KiwiConfigManager.getModsWithScreen(attribute));
				}
				Map<String, ConfigScreenFactory<?>> factories = Maps.newHashMap();
				for (String mod : mods) {
					factories.put(mod, _ -> ConfigScreens.createScreen(mod));
				}
				cachedFactories = Map.copyOf(factories);
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
