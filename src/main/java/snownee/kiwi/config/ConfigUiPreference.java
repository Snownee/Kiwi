package snownee.kiwi.config;

import java.util.Locale;

import net.minecraft.network.chat.Component;
import snownee.kiwi.util.LocalizableItem;

public enum ConfigUiPreference implements LocalizableItem {

	AUTO,
	JADE,
	CLOTH;

	@Override
	public Component getDisplayName() {
		return Component.translatable("kiwi.config.configUiPreference." + name().toLowerCase(Locale.ENGLISH));
	}

	@Override
	public Component getDescription() {
		return Component.translatable("kiwi.config.configUiPreference." + name().toLowerCase(Locale.ENGLISH) + ".desc");
	}
}
