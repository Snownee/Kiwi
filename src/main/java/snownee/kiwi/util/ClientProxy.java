package snownee.kiwi.util;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.neoforge.common.NeoForge;
import snownee.kiwi.contributor.ContributorsClient;
import snownee.kiwi.contributor.client.CosmeticLayer;
import snownee.kiwi.util.client.SmartKey;

@EventBusSubscriber
public class ClientProxy {
	public static void registerColors(Context context, List<Pair<Block, BlockColor>> blocksToAdd) {
		var modEventBus = context.modEventBus();
		if (!blocksToAdd.isEmpty()) {
			modEventBus.addListener((RegisterColorHandlersEvent.Block event) -> {
				for (var pair : blocksToAdd) {
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

	@SubscribeEvent
	public static void addLayers(EntityRenderersEvent.AddLayers event) {
		ImmutableMap.Builder<PlayerSkin.Model, CosmeticLayer> builder = ImmutableMap.builder();
		for (PlayerSkin.Model skin : event.getSkins()) {
			if (event.getSkin(skin) instanceof PlayerRenderer renderer) {
				CosmeticLayer layer = new CosmeticLayer(renderer);
				builder.put(skin, layer);
				renderer.addLayer(layer);
			}
		}
		CosmeticLayer.ALL_LAYERS = builder.build();
	}

	@SubscribeEvent
	public static void registerRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
		event.registerEntityModifier(
				PlayerRenderer.class, (player, state) -> {
					state.setRenderData(CosmeticLayer.COSMETIC_KEY, CosmeticLayer.getRendererOf(player));
				});
	}

	@SubscribeEvent
	public static void loggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
		ContributorsClient.changeCosmetic();
	}

	@SubscribeEvent
	public static void loggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
		ContributorsClient.clear();
	}

	@SubscribeEvent
	public static void keyInput(InputEvent.Key event) {
		ContributorsClient.onKeyInput(Minecraft.getInstance());
	}

	public record Context(boolean loading, IEventBus modEventBus) {
	}
}
