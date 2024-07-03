package snownee.kiwi.contributor;

import java.util.Map;
import java.util.Objects;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForge;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiClientConfig;
import snownee.kiwi.ModContext;
import snownee.kiwi.config.ConfigHandler;
import snownee.kiwi.config.KiwiConfigManager;
import snownee.kiwi.contributor.client.CosmeticLayer;
import snownee.kiwi.contributor.client.gui.CosmeticScreen;
import snownee.kiwi.contributor.network.CSetCosmeticPacket;
import snownee.kiwi.loader.event.InitEvent;
import snownee.kiwi.network.KPacketSender;
import snownee.kiwi.util.KUtil;

public class ContributorsClient extends AbstractModule {

	@Override
	protected void init(InitEvent event) {
		event.enqueueWork(() -> {
			IEventBus eventBus = Objects.requireNonNull(ModContext.get(Kiwi.ID).modContainer.getEventBus());
			eventBus.addListener((EntityRenderersEvent.AddLayers e) -> {
				for (PlayerSkin.Model skin : e.getSkins()) {
					if (e.getSkin(skin) instanceof PlayerRenderer renderer) {
						CosmeticLayer layer = new CosmeticLayer(renderer);
						CosmeticLayer.ALL_LAYERS.add(layer);
						renderer.addLayer(layer);
					}
				}
			});
			NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingIn e) -> {
				ContributorsClient.changeCosmetic();
			});
			NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut e) -> {
				Contributors.PLAYER_COSMETICS.clear();
				CosmeticLayer.ALL_LAYERS.forEach(l -> l.getCache().invalidateAll());
			});
			NeoForge.EVENT_BUS.addListener((InputEvent.Key e) -> onKeyInput(Minecraft.getInstance()));
		});
	}

	private static int hold;

	public static void onKeyInput(Minecraft mc) {
		if (mc.screen != null || mc.player == null || !mc.isWindowActive()) {
			return;
		}
		boolean K = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_K);
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
		Contributors.canPlayerUseCosmetic(getPlayerName(), cosmetic).thenAccept(bl -> {
			if (!bl) {
				ConfigHandler cfg = KiwiConfigManager.getHandler(KiwiClientConfig.class);
				KiwiClientConfig.contributorCosmetic = "";
				cfg.save();
				return;
			}
			KPacketSender.sendToServer(new CSetCosmeticPacket(cosmetic));
			if (cosmetic == null) {
				Contributors.PLAYER_COSMETICS.remove(getPlayerName());
			} else {
				Contributors.PLAYER_COSMETICS.put(getPlayerName(), cosmetic);
				Kiwi.LOGGER.info("Enabled contributor effect: {}", cosmetic);
			}
			CosmeticLayer.ALL_LAYERS.forEach(l -> l.getCache().invalidate(getPlayerName()));
		});
	}

	public static void changeCosmetic(Map<String, ResourceLocation> changes) {
		changes.forEach((k, v) -> {
			if (v == null) {
				Contributors.PLAYER_COSMETICS.remove(k);
			} else {
				Contributors.PLAYER_COSMETICS.put(k, v);
			}
		});
		CosmeticLayer.ALL_LAYERS.forEach(l -> l.getCache().invalidateAll(changes.keySet()));
	}

	private static String getPlayerName() {
		return Minecraft.getInstance().getUser().getName();
	}

}