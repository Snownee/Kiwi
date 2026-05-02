package snownee.kiwi.contributor;

import java.util.UUID;
import java.util.function.Function;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
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
import snownee.kiwi.network.KPacketSender;
import snownee.kiwi.util.KUtil;
import snownee.kiwi.util.client.SmartKey;

public class ContributorsClient extends AbstractModule {

	@Override
	protected void init(InitEvent event) {
		registerRenderer("2020q3", PlanetLayer::new);
		registerRenderer("2020q4", FoxTailLayer::new);
		registerRenderer("xmas", SantaHatLayer::new);
		registerRenderer("sunny_milk", SunnyMilkLayer::new);
	}

	private static void registerRenderer(String id, Function<RenderLayerParent<AvatarRenderState, PlayerModel>, CosmeticLayer> creator) {
		CosmeticLayer.registerRenderer(Identifier.fromNamespaceAndPath("snownee", id), creator);
	}

	private static int hold;

	public static void onKeyInput(Minecraft mc) {
		if (!KiwiClientConfig.cosmeticScreenKeybind || mc.screen != null || mc.player == null || !mc.isWindowActive()) {
			return;
		}
		boolean K = InputConstants.isKeyDown(mc.getWindow(), InputConstants.KEY_K);
		if (!K || SmartKey.hasAltDown() || SmartKey.hasControlDown() || SmartKey.hasShiftDown()) {
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
		CosmeticLayer.getCache().clear();
	}

	public static UUID getSelfUUID() {
		return Minecraft.getInstance().getUser().getProfileId();
	}

	public static String getSelfName() {
		return Minecraft.getInstance().getUser().getName();
	}

}
