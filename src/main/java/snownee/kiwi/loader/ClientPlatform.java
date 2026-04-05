package snownee.kiwi.loader;

import java.util.Locale;

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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

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
}
