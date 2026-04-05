package snownee.kiwi.util;

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import snownee.kiwi.util.client.SmartKey;

public class ClientProxy {
	public static void registerColors(Context context, List<Pair<Block, List<BlockTintSource>>> blocksToAdd) {
		var modEventBus = context.modEventBus();
		if (!blocksToAdd.isEmpty()) {
			modEventBus.addListener((RegisterColorHandlersEvent.Block event) -> {
				BlockColors blockColors = event.getBlockColors();
				for (var pair : blocksToAdd) {
					blockColors.register(pair.getSecond(), pair.getFirst());
				}
			});
		}
	}

	public static void pushScreen(Minecraft mc, Screen screen) {
		mc.pushGuiLayer(screen);
	}

	@Nullable
	public static Slot getSlotUnderMouse(AbstractContainerScreen<?> containerScreen) {
		return containerScreen.getSlotUnderMouse();
	}

	public static void afterRegisterSmartKey(SmartKey smartKey) {
		Preconditions.checkNotNull(smartKey);
		var forgeEventBus = NeoForge.EVENT_BUS;
		forgeEventBus.addListener((ClientTickEvent.Post event) -> {
			smartKey.tick();
		});
		forgeEventBus.addListener((ScreenEvent.MouseButtonPressed.Pre event) -> {
			if (smartKey.matchesMouse(event.getButton()) && smartKey.setDownWithResult(true)) {
				event.setCanceled(true);
			}
		});
		forgeEventBus.addListener((ScreenEvent.MouseButtonReleased.Pre event) -> {
			if (smartKey.matchesMouse(event.getButton()) && smartKey.setDownWithResult(false)) {
				event.setCanceled(true);
			}
		});
		forgeEventBus.addListener((ScreenEvent.KeyPressed.Pre event) -> {
			if (smartKey.matches(event.getKeyCode(), event.getScanCode()) && smartKey.setDownWithResult(true)) {
				event.setCanceled(true);
			}
		});
		forgeEventBus.addListener((ScreenEvent.KeyReleased.Pre event) -> {
			if (smartKey.matches(event.getKeyCode(), event.getScanCode()) && smartKey.setDownWithResult(false)) {
				event.setCanceled(true);
			}
		});
	}

	public record Context(boolean loading, IEventBus modEventBus) {
	}
}
