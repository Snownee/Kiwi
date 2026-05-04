package snownee.kiwi.loader;

import java.util.Locale;
import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForge;
import snownee.kiwi.Kiwi;
import snownee.kiwi.ModContext;
import snownee.kiwi.contributor.ContributorsClient;
import snownee.kiwi.contributor.client.CosmeticLayer;
import snownee.kiwi.loader.event.InitEvent;

public final class ClientPlatform {
	private ClientPlatform() {
	}

	public static <E extends Entity> void registerEntityRenderer(
			EntityType<? extends E> entityType,
			EntityRendererProvider<E> entityRendererFactory) {
		// Entity renderers are registered via NeoForge events in this branch.
	}

	public static <T extends BlockEntity, S extends BlockEntityRenderState> void registerBlockEntityRenderer(
			BlockEntityType<? extends T> blockEntityType,
			BlockEntityRendererProvider<T, S> blockEntityRendererProvider) {
		BlockEntityRenderers.register(blockEntityType, blockEntityRendererProvider);
	}

	public static <T extends ParticleOptions> void registerParticleType(ParticleType<T> type, ParticleProvider<T> factory) {
		// Particle providers are registered via NeoForge events in this branch.
	}

	public static Locale getLocale() {
		return Minecraft.getInstance().getLocale();
	}

	public static void registerContributorsListeners(InitEvent event) {
		event.enqueueWork(() -> {
			IEventBus eventBus = Objects.requireNonNull(ModContext.get(Kiwi.ID).modContainer.getEventBus());
			eventBus.addListener((EntityRenderersEvent.AddLayers e) -> {
				for (PlayerModelType skin : e.getSkins()) {
					var renderer = e.getPlayerRenderer(skin);
					if (renderer == null) continue;
					var layer = new CosmeticLayer(renderer);
					CosmeticLayer.ALL_LAYERS.put(skin, layer);
					renderer.addLayer(layer);
				}
			});
			NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingIn e) -> {
				ContributorsClient.changeCosmetic();
			});
			NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut e) -> ContributorsClient.clear());
			NeoForge.EVENT_BUS.addListener((InputEvent.Key e) -> ContributorsClient.onKeyInput(Minecraft.getInstance()));
		});
	}
}
