package snownee.kiwi.customization.duck;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import snownee.kiwi.customization.block.KBlockSettings;

public interface KBlockProperties {
	@Nullable
	@ApiStatus.NonExtendable
	KBlockSettings kiwi$getSettings();

	@ApiStatus.NonExtendable
	void kiwi$setSettings(KBlockSettings settings);
}
