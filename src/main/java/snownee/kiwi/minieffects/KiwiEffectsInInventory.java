package snownee.kiwi.minieffects;

import java.util.List;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.gui.navigation.ScreenRectangle;

public interface KiwiEffectsInInventory {
	@Nullable ScreenRectangle kiwi$buttonArea();

	List<ScreenRectangle> kiwi$areas();

	boolean kiwi$isExpanded();

	void kiwi$setExpanded(boolean bl);
}
