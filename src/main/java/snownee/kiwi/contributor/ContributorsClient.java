package snownee.kiwi.contributor;

import java.util.Objects;
import java.util.UUID;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
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
import snownee.kiwi.contributor.network.SSyncCosmeticPacket;
import snownee.kiwi.loader.event.InitEvent;
import snownee.kiwi.network.KPacketSender;
import snownee.kiwi.util.KUtil;

public class ContributorsClient extends AbstractModule {

	@Override
	protected void init(InitEvent event) {
		event.enqueueWork(() -> {
			IEventBus eventBus = Objects.requireNonNull(ModContext.get(Kiwi.ID).modContainer.getEventBus());
			eventBus.addListener((EntityRenderersEvent.AddLayers e) -> {
				try {
					Iterable<?> skins = (Iterable<?>) e.getClass().getMethod("getSkins").invoke(e);
					for (Object skin : skins) {
						java.lang.reflect.Method getter;
						try {
							getter = e.getClass().getMethod("getSkin", skin.getClass());
						} catch (NoSuchMethodException ignored) {
							getter = e.getClass().getMethod("getPlayerRenderer", skin.getClass());
						}
						AvatarRenderer<?> renderer = (AvatarRenderer<?>) getter.invoke(e, skin);
						if (renderer != null) {
							CosmeticLayer layer = new CosmeticLayer(renderer);
							CosmeticLayer.ALL_LAYERS.add(layer);
							renderer.addLayer(layer);
						}
					}
				} catch (ReflectiveOperationException ex) {
					Kiwi.LOGGER.error("Failed to attach contributor cosmetic layers", ex);
				}
			});
			NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingIn e) -> {
				ContributorsClient.changeCosmetic();
			});
			NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut e) -> clear());
			NeoForge.EVENT_BUS.addListener((InputEvent.Key e) -> onKeyInput(Minecraft.getInstance()));
		});
	}

	private static int hold;

	private static boolean hasControlDown(Minecraft mc) {
		var window = mc.getWindow();
		return InputConstants.isKeyDown(window, 341)
				|| InputConstants.isKeyDown(window, 345)
				|| InputConstants.isKeyDown(window, 343)
				|| InputConstants.isKeyDown(window, 347);
	}

	private static boolean hasShiftDown(Minecraft mc) {
		var window = mc.getWindow();
		return InputConstants.isKeyDown(window, 340) || InputConstants.isKeyDown(window, 344);
	}

	private static boolean hasAltDown(Minecraft mc) {
		var window = mc.getWindow();
		return InputConstants.isKeyDown(window, 342) || InputConstants.isKeyDown(window, 346);
	}

	public static void onKeyInput(Minecraft mc) {
		if (!KiwiClientConfig.cosmeticScreenKeybind || mc.screen != null || mc.player == null || !mc.isWindowActive()) {
			return;
		}
		boolean K = InputConstants.isKeyDown(mc.getWindow(), InputConstants.KEY_K);
		if (!K || hasAltDown(mc) || hasControlDown(mc) || hasShiftDown(mc)) {
			hold = 0;
			return;
		}
		if (++hold == 30) {
			CosmeticScreen screen = new CosmeticScreen();
			mc.setScreen(screen);
		}
	}

	public static void changeCosmetic() {
		Identifier id = KUtil.RL(KiwiClientConfig.contributorCosmetic);
		if (id != null && id.getPath().isEmpty()) {
			id = null;
		}
		Identifier cosmetic = id;
		Contributors.canPlayerUseCosmetic(getSelfName(), cosmetic).thenAccept(bl -> {
			if (!bl) {
				ConfigHandler cfg = KiwiConfigManager.getHandler(KiwiClientConfig.class);
				KiwiClientConfig.contributorCosmetic = "";
				cfg.save();
				return;
			}
			KPacketSender.sendToServer(new CSetCosmeticPacket(cosmetic));
			if (cosmetic == null) {
				Contributors.PLAYER_COSMETICS.remove(getSelfName());
			} else {
				Contributors.PLAYER_COSMETICS.put(getSelfName(), cosmetic);
				Kiwi.LOGGER.info("Enabled contributor effect: {}", cosmetic);
			}
			CosmeticLayer.ALL_LAYERS.forEach(l -> l.getCache().invalidate(getSelfName()));
		});
	}

	public static void changeCosmetic(SSyncCosmeticPacket changes) {
		if (changes.add().isEmpty() && changes.remove().isEmpty()) {
			clear();
			return;
		}
		Contributors.PLAYER_COSMETICS.putAll(changes.add());
		for (String s : changes.remove()) {
			Contributors.PLAYER_COSMETICS.remove(s);
		}
		CosmeticLayer.ALL_LAYERS.forEach(l -> {
			l.getCache().invalidateAll(changes.add().keySet());
			l.getCache().invalidateAll(changes.remove());
		});
	}

	public static void clear() {
		Contributors.PLAYER_COSMETICS.clear();
		CosmeticLayer.ALL_LAYERS.forEach(l -> l.getCache().invalidateAll());
	}

	public static UUID getSelfUUID() {
		return Minecraft.getInstance().getUser().getProfileId();
	}

	public static String getSelfName() {
		return Minecraft.getInstance().getUser().getName();
	}

}