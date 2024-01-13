package snownee.kiwi.contributor;

import java.util.Map;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiClientConfig;
import snownee.kiwi.config.ConfigHandler;
import snownee.kiwi.config.KiwiConfigManager;
import snownee.kiwi.contributor.client.CosmeticLayer;
import snownee.kiwi.contributor.client.gui.CosmeticScreen;
import snownee.kiwi.contributor.network.CSetCosmeticPacket;
import snownee.kiwi.loader.event.InitEvent;
import snownee.kiwi.util.Util;

public class ContributorsClient extends AbstractModule {

	@Override
	protected void init(InitEvent event) {
		event.enqueueWork(() -> {
			LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
				if (entityRenderer instanceof PlayerRenderer) {
					CosmeticLayer layer = new CosmeticLayer((PlayerRenderer) entityRenderer);
					CosmeticLayer.ALL_LAYERS.add(layer);
					registrationHelper.register(layer);
				}
			});
			ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
				ContributorsClient.changeCosmetic();
			});
			ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
				Contributors.PLAYER_COSMETICS.clear();
				CosmeticLayer.ALL_LAYERS.forEach(l -> l.getCache().invalidateAll());
			});
			ClientTickEvents.END_CLIENT_TICK.register(ContributorsClient::onKeyInput);
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
		ResourceLocation id = Util.RL(KiwiClientConfig.contributorCosmetic);
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
			CSetCosmeticPacket.send(cosmetic);
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
