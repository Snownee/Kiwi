package snownee.kiwi.contributor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.mixin.client.rendering.LivingEntityRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiClientConfig;
import snownee.kiwi.config.ConfigHandler;
import snownee.kiwi.config.KiwiConfigManager;
import snownee.kiwi.contributor.client.CosmeticLayer;
import snownee.kiwi.contributor.client.gui.CosmeticScreen;
import snownee.kiwi.contributor.impl.client.layer.FoxTailLayer;
import snownee.kiwi.contributor.impl.client.layer.PlanetLayer;
import snownee.kiwi.contributor.impl.client.layer.SantaHatLayer;
import snownee.kiwi.contributor.impl.client.layer.SunnyMilkLayer;
import snownee.kiwi.contributor.network.CSetCosmeticPacket;
import snownee.kiwi.contributor.network.SSyncCosmeticPacket;
import snownee.kiwi.loader.event.InitEvent;
import snownee.kiwi.mixin.client.EntityRenderDispatcherAccess;
import snownee.kiwi.network.KPacketSender;
import snownee.kiwi.util.KUtil;

public class ContributorsClient extends AbstractModule {

	@Override
	protected void init(InitEvent event) {
		event.enqueueWork(() -> {
			CosmeticLayer.registerRenderer(Kiwi.id("2020q3"), PlanetLayer::new);
			CosmeticLayer.registerRenderer(Kiwi.id("2020q4"), FoxTailLayer::new);
			CosmeticLayer.registerRenderer(Kiwi.id("xmas"), SantaHatLayer::new);
			CosmeticLayer.registerRenderer(Kiwi.id("sunny_milk"), SunnyMilkLayer::new);

			ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener() {
				private static final ResourceLocation ID = Kiwi.id("contributors");

				@Override
				public ResourceLocation getFabricId() {
					return ID;
				}

				@Override
				public CompletableFuture<Void> reload(
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
			ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> clear());
			ClientTickEvents.END_CLIENT_TICK.register(ContributorsClient::onKeyInput);
		});
	}

	private static int hold;

	public static void onKeyInput(Minecraft mc) {
		if (!KiwiClientConfig.cosmeticScreenKeybind || mc.screen != null || mc.player == null || !mc.isWindowActive()) {
			return;
		}
		boolean K = InputConstants.isKeyDown(mc.getWindow().getWindow(), InputConstants.KEY_K);
		if (!K || Screen.hasAltDown() || Screen.hasControlDown() || Screen.hasShiftDown()) {
			hold = 0;
			return;
		}
		if (++hold == 30) {
			CosmeticScreen screen = new CosmeticScreen();
			mc.setScreen(screen);
		}
	}

	public static void changeCosmetic() {
		ResourceLocation id = KUtil.RL(KiwiClientConfig.contributorCosmetic);
		if (id != null && id.getPath().isEmpty()) {
			id = null;
		}
		ResourceLocation cosmetic = id;
		Contributors.canPlayerUseCosmetic(getSelfName(), cosmetic).thenAccept(bl -> {
			if (!bl) {
				ConfigHandler cfg = KiwiConfigManager.getHandler(KiwiClientConfig.class);
				KiwiClientConfig.contributorCosmetic = "";
				cfg.save();
				return;
			}
			KPacketSender.sendToServer(new CSetCosmeticPacket(cosmetic));
			if (cosmetic == null) {
				Contributors.PLAYER_COSMETICS.remove(getSelfUUID());
			} else {
				Contributors.PLAYER_COSMETICS.put(getSelfUUID(), cosmetic);
				Kiwi.LOGGER.info("Enabled contributor effect: {}", cosmetic);
			}
			CosmeticLayer.getCache().remove(getSelfUUID());
		});
	}

	public static void changeCosmetic(SSyncCosmeticPacket changes) {
		if (changes.add().isEmpty() && changes.remove().isEmpty()) {
			clear();
			return;
		}
		Contributors.PLAYER_COSMETICS.putAll(changes.add());
		for (UUID s : changes.remove()) {
			Contributors.PLAYER_COSMETICS.remove(s);
		}
		changes.add().keySet().forEach(CosmeticLayer.getCache()::remove);
		changes.remove().forEach(CosmeticLayer.getCache()::remove);
	}

	public static void clear() {
		Contributors.PLAYER_COSMETICS.clear();
		CosmeticLayer.ALL_LAYERS.forEach(l -> l.getCache().invalidateAll());
		CosmeticLayer.getCache().clear();
	}

	private static UUID getSelfUUID() {
		return Minecraft.getInstance().getUser().getProfileId();
	}

	private static String getSelfName() {
		return Minecraft.getInstance().getUser().getName();
	}

}
