package snownee.kiwi.config;

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.screens.Screen;
import snownee.kiwi.KiwiClientConfig;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.util.ClientProxy;

/**
 * Selects the available config screen implementations in the order requested by the player.
 */
public final class ConfigScreens {

	private ConfigScreens() {
	}

	public static List<ConfigLibAttributes> attributes() {
		List<ConfigLibAttributes> attributes = Lists.newArrayListWithExpectedSize(2);
		if (KiwiClientConfig.configUiPreference == ConfigUiPreference.JADE) {
			addJade(attributes);
			addCloth(attributes);
		} else {
			addCloth(attributes);
			addJade(attributes);
		}
		return attributes;
	}

	public static @Nullable Screen createScreen(String mod) {
		for (ConfigLibAttributes attributes : attributes()) {
			Screen screen = attributes.screenFactory().apply(mod);
			if (screen != null) {
				return screen;
			}
		}
		return null;
	}

	private static void addJade(List<ConfigLibAttributes> attributes) {
		if (Platform.isModLoaded("jade")) {
			attributes.add(JadeConfigIntegration.attributes());
		}
	}

	private static void addCloth(List<ConfigLibAttributes> attributes) {
		if (ClientProxy.clothConfig) {
			attributes.add(ClothConfigIntegration.attributes());
		}
	}
}
