package snownee.kiwi.customization.builder;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import snownee.kiwi.customization.CustomizationClient;

public class DebugEntryBuilderMode implements DebugScreenEntry {
	@Override
	public void display(
			DebugScreenDisplayer displayer,
			@Nullable Level serverOrClientLevel,
			@Nullable LevelChunk clientChunk,
			@Nullable LevelChunk serverChunk) {
		if (!BuildersButton.isBuilderModeOn()) {
			return;
		}
		String line = I18n.get("kiwi.builder_mode.debug_entry");
		if (I18n.exists("kiwi.builder_mode.debug_key_hint")) {
			line = line + " " + I18n.get(
					"kiwi.builder_mode.debug_key_hint",
					Objects.requireNonNull(CustomizationClient.buildersButtonKey).getTranslatedKeyMessage().getString());
		}
		displayer.addPriorityLine(line);
	}

	@Override
	public boolean isAllowed(boolean reducedDebugInfo) {
		return true;
	}
}
