package snownee.kiwi.loader;

import java.util.Locale;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.mixin.client.rendering.LivingEntityRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import snownee.kiwi.Kiwi;
import snownee.kiwi.RenderLayerEnum;
import snownee.kiwi.client.TooltipEvents;
import snownee.kiwi.command.ClientCommandContext;
import snownee.kiwi.command.KalcCommand;
import snownee.kiwi.command.KiwiClientCommand;
import snownee.kiwi.contributor.ContributorsClient;
import snownee.kiwi.contributor.client.CosmeticLayer;
import snownee.kiwi.minieffects.EffectRenderingScreen;
import snownee.kiwi.minieffects.MiniEffects;
import snownee.kiwi.mixin.client.EntityRenderDispatcherAccess;

public final class ClientPlatform implements ClientModInitializer {
	public static final boolean hasMiniEffects = Platform.isModLoaded("minieffects") || !Platform.isProduction();
	public static final Identifier HIGH = Kiwi.id("high");
	public static final Identifier LOW = Kiwi.id("low");

	public static <E extends Entity> void registerEntityRenderer(
			EntityType<? extends E> entityType,
			EntityRendererProvider<E> entityRendererFactory) {
		EntityRenderers.register(entityType, entityRendererFactory);
	}

	public static <T extends BlockEntity, S extends BlockEntityRenderState> void registerBlockEntityRenderer(
			BlockEntityType<? extends T> blockEntityType,
			BlockEntityRendererProvider<T, S> blockEntityRendererProvider) {
		BlockEntityRenderers.register(blockEntityType, blockEntityRendererProvider);
	}

	public static <T extends ParticleOptions> void registerParticleType(ParticleType<T> type, ParticleProvider<T> factory) {
		ParticleProviderRegistry.getInstance().register(type, factory);
	}

	public static void setRenderType(Block block, ChunkSectionLayer layer) {
		ItemBlockRenderTypes.TYPE_BY_BLOCK.put(block, layer);
	}

	public static Locale getLocale() {
		String[] langSplit = Minecraft.getInstance().getLanguageManager().getSelected().split("_", 2);
		return langSplit.length == 1 ? Locale.of(langSplit[0]) : Locale.of(langSplit[0], langSplit[1]);
	}

	@Override
	public void onInitializeClient() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			ClientCommandContext<FabricClientCommandSource> context = new ClientCommandContext<>(registryAccess);
			dispatcher.register(KiwiClientCommand.create(context));
			dispatcher.register(KalcCommand.create(context));
		});
		ClientLifecycleEvents.CLIENT_STARTED.register(Kiwi::clientInit);

		RenderLayerEnum.CUTOUT.value = ChunkSectionLayer.CUTOUT;
		RenderLayerEnum.TRANSLUCENT.value = ChunkSectionLayer.TRANSLUCENT;

		ItemTooltipCallback.EVENT.addPhaseOrdering(HIGH, Event.DEFAULT_PHASE);
		ItemTooltipCallback.EVENT.addPhaseOrdering(Event.DEFAULT_PHASE, LOW);
		ItemTooltipCallback.EVENT.register(HIGH, (stack, context, type, lines) -> TooltipEvents.globalTooltip(stack, lines, type));
		ItemTooltipCallback.EVENT.register(LOW, (stack, context, type, lines) -> TooltipEvents.debugTooltip(stack, lines, type));

		ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(
				Kiwi.id("contributors"), (currentReload, taskExecutor, barrier, reloadExecutor) -> {
					return barrier.wait(Unit.INSTANCE).thenRunAsync(
							() -> ((EntityRenderDispatcherAccess) Minecraft.getInstance().getEntityRenderDispatcher())
									.getPlayerRenderers()
									.forEach((skin, renderer) -> {
										CosmeticLayer layer = new CosmeticLayer(renderer);
										CosmeticLayer.ALL_LAYERS.put(skin, layer);
										((LivingEntityRendererAccessor<AvatarRenderState, PlayerModel>) renderer).callAddLayer(layer);
									}), reloadExecutor);
				});

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ContributorsClient.changeCosmetic());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ContributorsClient.clear());
		ClientTickEvents.END_CLIENT_TICK.register(ContributorsClient::onKeyInput);
		if (hasMiniEffects) {
			ScreenEvents.AFTER_INIT.register((_, screen, _, _) -> {
				if (!(screen instanceof EffectRenderingScreen)) {
					return;
				}
				ScreenMouseEvents.allowMouseClick(screen).register((screen1, event) -> {
					return MiniEffects.allowClick((EffectRenderingScreen) screen1, event);
				});
				ScreenMouseEvents.afterMouseClick(screen).register((screen1, event, consumed) -> {
					return MiniEffects.afterClick((EffectRenderingScreen) screen1, event);
				});
			});
		}

		// a hack to make sure our mod is loaded after all other mods,
		// so that other mods can call `enableDataModule` in their `onInitialize` method
		Kiwi.onInitialize();
	}
}
