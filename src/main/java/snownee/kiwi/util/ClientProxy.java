package snownee.kiwi.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.jetbrains.annotations.NotNull;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.mixin.client.rendering.LivingEntityRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
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
				return CompletableFuture.runAsync(() -> {
					((EntityRenderDispatcherAccess) Minecraft.getInstance().getEntityRenderDispatcher())
							.getPlayerRenderers()
							.forEach((skin, renderer) -> {
								CosmeticLayer layer = new CosmeticLayer((PlayerRenderer) renderer);
								CosmeticLayer.ALL_LAYERS.put(skin, layer);
								((LivingEntityRendererAccessor<PlayerRenderState, PlayerModel>) renderer).callAddFeature(layer);
							});
				});
			}
		});

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ContributorsClient.changeCosmetic());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ContributorsClient.clear());
		ClientTickEvents.END_CLIENT_TICK.register(ContributorsClient::onKeyInput);
	}
}
