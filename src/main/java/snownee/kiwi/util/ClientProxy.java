package snownee.kiwi.util;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;

import fabricscreenlayers.ScreenLayerManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ClientProxy {
	public static void registerColors(Context context, List<Pair<Block, BlockColor>> blocksToAdd, List<Pair<Item, ItemColor>> itemsToAdd) {
		for (var pair : blocksToAdd) {
			ColorProviderRegistry.BLOCK.register(pair.getSecond(), pair.getFirst());
		}
		for (var pair : itemsToAdd) {
			ColorProviderRegistry.ITEM.register(pair.getSecond(), pair.getFirst());
		}
	}

	public static void pushScreen(Minecraft mc, Screen screen) {
		ScreenLayerManager.pushLayer(screen);
	}

	@Nullable
	public static Slot getSlotUnderMouse(AbstractContainerScreen<?> containerScreen) {
		return containerScreen.hoveredSlot;
	}

	public static void afterRegisterSmartKey(SmartKey smartKey) {
		Preconditions.checkNotNull(smartKey);
		ClientTickEvents.END_CLIENT_TICK.register(mc -> smartKey.tick());
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			ScreenMouseEvents.allowMouseClick(screen).register((Screen screen1, double mouseX, double mouseY, int button) -> {
				if (smartKey.matchesMouse(button) && smartKey.setDownWithResult(true)) {
					return false;
				}
				return true;
			});
			ScreenMouseEvents.allowMouseRelease(screen).register((Screen screen1, double mouseX, double mouseY, int button) -> {
				if (smartKey.matchesMouse(button) && smartKey.setDownWithResult(false)) {
					return false;
				}
				return true;
			});
			ScreenKeyboardEvents.allowKeyPress(screen).register((Screen screen1, int keyCode, int scanCode, int modifiers) -> {
				if (smartKey.matches(keyCode, scanCode) && smartKey.setDownWithResult(true)) {
					return false;
				}
				return true;
			});
			ScreenKeyboardEvents.allowKeyRelease(screen).register((Screen screen1, int keyCode, int scanCode, int modifiers) -> {
				if (smartKey.matches(keyCode, scanCode) && smartKey.setDownWithResult(false)) {
					return false;
				}
				return true;
			});
		});
	}

	public record Context(boolean loading) {
	}
}
