package snownee.kiwi.loader;

import java.util.Locale;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import snownee.kiwi.Kiwi;
import snownee.kiwi.command.ClientCommandContext;
import snownee.kiwi.command.KalcCommand;
import snownee.kiwi.command.KiwiClientCommand;

public final class ClientPlatform {
	private ClientPlatform() {
	}

	public static void init() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			ClientCommandContext<FabricClientCommandSource> context = new ClientCommandContext<>(registryAccess);
			dispatcher.register(KiwiClientCommand.create(context));
			dispatcher.register(KalcCommand.create(context));
		});
		ClientLifecycleEvents.CLIENT_STARTED.register(Kiwi::clientInit);
	}

	public static <E extends Entity> void registerEntityRenderer(
			EntityType<? extends E> entityType,
			EntityRendererProvider<E> entityRendererFactory) {
		EntityRendererRegistry.register(entityType, entityRendererFactory);
	}

	public static <T extends BlockEntity> void registerBlockEntityRenderer(
			BlockEntityType<? extends T> blockEntityType,
			BlockEntityRendererProvider<T> blockEntityRendererProvider) {
		BlockEntityRenderers.register(blockEntityType, blockEntityRendererProvider);
	}

	public static <T extends ParticleOptions> void registerParticleType(ParticleType<T> type, ParticleProvider<T> factory) {
		ParticleFactoryRegistry.getInstance().register(type, factory);
	}

	public static void setRenderType(Block block, ChunkSectionLayer layer) {
		ItemBlockRenderTypes.TYPE_BY_BLOCK.put(block, layer);
	}

	public static Locale getLocale() {
		String[] langSplit = Minecraft.getInstance().getLanguageManager().getSelected().split("_", 2);
		return langSplit.length == 1 ? new Locale(langSplit[0]) : new Locale(langSplit[0], langSplit[1]);
	}
}
