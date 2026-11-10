package snownee.kiwi.config;

import java.util.List;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import snownee.kiwi.config.gui.KiwiConfigScreen;

/**
 * Optional integration that reuses Jade's options screen for Kiwi's configuration UI. This class
 * must only be loaded when Jade is present.
 */
public final class JadeConfigIntegration {

	private static final ConfigLibAttributes ATTRIBUTES = new ConfigLibAttributes(
			"jade",
			namespace -> create(Minecraft.getInstance().gui.screen(), namespace),
			true,
			false,
			true);

	private JadeConfigIntegration() {
	}

	public static ConfigLibAttributes attributes() {
		return ATTRIBUTES;
	}

	public static @Nullable Screen create(@Nullable Screen parent, String namespace) {
		List<ConfigHandler> configs = KiwiConfigManager.getModHandlersWithScreen(namespace, ATTRIBUTES);
		if (configs.isEmpty()) {
			return null;
		}
		return new KiwiConfigScreen(parent, namespace, configs);
	}
}
