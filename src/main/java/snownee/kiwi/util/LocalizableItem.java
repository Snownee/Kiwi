package snownee.kiwi.util;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;

public interface LocalizableItem {

	Component getDisplayName();

	@Nullable
	default Component getDescription() {
		return null;
	}

}
