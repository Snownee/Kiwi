package snownee.kiwi.util;

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.mixin.client.rendering.LivingEntityRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.Unit;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.Block;
import snownee.kiwi.Kiwi;
import snownee.kiwi.contributor.ContributorsClient;
import snownee.kiwi.contributor.client.CosmeticLayer;
import snownee.kiwi.mixin.client.EntityRenderDispatcherAccess;
import snownee.kiwi.util.client.SmartKey;

public final class ClientProxy {
	public static void init() {
		ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(
				Kiwi.id("contributors"), (currentReload, taskExecutor, barrier, reloadExecutor) -> {
					return barrier.wait(Unit.INSTANCE).thenRunAsync(
							() -> ((EntityRenderDispatcherAccess) Minecraft.getInstance().getEntityRenderDispatcher())
									.getPlayerRenderers()
									.forEach((skin, renderer) -> {
										CosmeticLayer layer = new CosmeticLayer(renderer);
										CosmeticLayer.ALL_LAYERS.put(skin, layer);
										((LivingEntityRendererAccessor<AvatarRenderState, PlayerModel>) renderer).callAddFeature(layer);
									}), backgroundExecutor);
				});

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ContributorsClient.changeCosmetic());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ContributorsClient.clear());
		ClientTickEvents.END_CLIENT_TICK.register(ContributorsClient::onKeyInput);
	}

	public static void registerColors(Context context, List<Pair<Block, BlockColor>> blocksToAdd) {
		for (var pair : blocksToAdd) {
			ColorProviderRegistry.BLOCK.register(pair.getSecond(), pair.getFirst());
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
				if (smartKey.matchesMouse(event) && smartKey.setDownWithResult(true)) {
					return false;
				}
				return true;
			});
			ScreenMouseEvents.allowMouseRelease(screen).register((_, event) -> {
				if (smartKey.matchesMouse(event) && smartKey.setDownWithResult(false)) {
					return false;
				}
				return true;
			});
			ScreenKeyboardEvents.allowKeyPress(screen).register((_, event) -> {
				if (smartKey.matches(event) && smartKey.setDownWithResult(true)) {
					return false;
				}
				return true;
			});
			ScreenKeyboardEvents.allowKeyRelease(screen).register((_, event) -> {
				if (smartKey.matches(event) && smartKey.setDownWithResult(false)) {
					return false;
				}
				return true;
			});
		});
	}

	public record Context(boolean loading) {
	}
}
