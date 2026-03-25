package snownee.kiwi.util;

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.Block;
import snownee.kiwi.util.client.SmartKey;

public final class ClientProxy {

	public static void registerColors(Context context, List<Pair<Block, List<BlockTintSource>>> blocksToAdd) {
		for (var pair : blocksToAdd) {
			BlockColorRegistry.register(pair.getSecond(), pair.getFirst());
		}
	}

	@Nullable
	public static Slot getSlotUnderMouse(AbstractContainerScreen<?> containerScreen) {
		return containerScreen.hoveredSlot;
	}

	public static void afterRegisterSmartKey(SmartKey smartKey) {
		Preconditions.checkNotNull(smartKey);
		ClientTickEvents.END_CLIENT_TICK.register(_ -> smartKey.tick());
		ScreenEvents.AFTER_INIT.register((_, screen, _, _) -> {
			ScreenMouseEvents.allowMouseClick(screen).register((_, event) -> {
				return !smartKey.matchesMouse(event) || !smartKey.setDownWithResult(true);
			});
			ScreenMouseEvents.allowMouseRelease(screen).register((_, event) -> {
				return !smartKey.matchesMouse(event) || !smartKey.setDownWithResult(false);
			});
			ScreenKeyboardEvents.allowKeyPress(screen).register((_, event) -> {
				return !smartKey.matches(event) || !smartKey.setDownWithResult(true);
			});
			ScreenKeyboardEvents.allowKeyRelease(screen).register((_, event) -> {
				return !smartKey.matches(event) || !smartKey.setDownWithResult(false);
			});
		});
	}

	public record Context(boolean loading) {}
}
