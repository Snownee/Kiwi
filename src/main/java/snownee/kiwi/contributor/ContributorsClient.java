package snownee.kiwi.contributor;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import snownee.kiwi.contributor.client.CosmeticLayer;
import snownee.kiwi.contributor.client.gui.CosmeticScreen;

@Environment(EnvType.CLIENT)
public class ContributorsClient {

	public static void init() {
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
			if (entityRenderer instanceof PlayerRenderer) {
				CosmeticLayer layer = new CosmeticLayer((PlayerRenderer) entityRenderer);
				CosmeticLayer.ALL_LAYERS.add(layer);
				registrationHelper.register(layer);
			}
		});
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			Contributors.changeCosmetic();
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			Contributors.PLAYER_COSMETICS.clear();
			CosmeticLayer.ALL_LAYERS.forEach(l -> l.getCache().invalidateAll());
		});
		ClientTickEvents.END_CLIENT_TICK.register(ContributorsClient::onKeyInput);
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

}
