package snownee.kiwi.util;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public class ClientProxy {
	public static void registerColors(Context context, List<Pair<Block, BlockColor>> blocksToAdd, List<Pair<Item, ItemColor>> itemsToAdd) {
		var modEventBus = context.modEventBus();
		if (!blocksToAdd.isEmpty()) {
			modEventBus.addListener((RegisterColorHandlersEvent.Block event) -> {
				for (var pair : blocksToAdd) {
					event.register(pair.getSecond(), pair.getFirst());
				}
			});
		}
		if (!itemsToAdd.isEmpty()) {
			modEventBus.addListener((RegisterColorHandlersEvent.Item event) -> {
				for (var pair : itemsToAdd) {
					event.register(pair.getSecond(), pair.getFirst());
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

	public record Context(boolean loading, IEventBus modEventBus) {
	}
}
