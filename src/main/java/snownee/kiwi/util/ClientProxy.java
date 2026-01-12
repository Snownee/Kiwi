package snownee.kiwi.util;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.mixin.client.rendering.LivingEntityRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.Block;
import snownee.kiwi.Kiwi;
import snownee.kiwi.contributor.ContributorsClient;
import snownee.kiwi.contributor.client.CosmeticLayer;
import snownee.kiwi.mixin.client.EntityRenderDispatcherAccess;

public final class ClientProxy {
	public static void init() {
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener() {
			private static final ResourceLocation ID = Kiwi.id("contributors");

			@Override
			public ResourceLocation getFabricId() {
				return ID;
			}

			@Override
			public @NotNull CompletableFuture<Void> reload(
					PreparationBarrier barrier,
					ResourceManager manager,
					Executor backgroundExecutor,
					Executor gameExecutor) {
				return barrier.wait(Unit.INSTANCE).thenRunAsync(
						() -> ((EntityRenderDispatcherAccess) Minecraft.getInstance().getEntityRenderDispatcher())
								.getPlayerRenderers()
								.forEach((skin, renderer) -> {
									CosmeticLayer layer = new CosmeticLayer((PlayerRenderer) renderer);
									CosmeticLayer.ALL_LAYERS.put(skin, layer);
									((LivingEntityRendererAccessor<PlayerRenderState, PlayerModel>) renderer).callAddFeature(layer);
								}), backgroundExecutor);
			}
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
