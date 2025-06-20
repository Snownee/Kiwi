package snownee.kiwi.contributor;

import java.util.Objects;
import java.util.UUID;

import com.google.common.collect.ImmutableMap;
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
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.neoforge.common.NeoForge;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiClientConfig;
import snownee.kiwi.ModContext;
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

public class ContributorsClient extends AbstractModule {

	@Override
	protected void init(InitEvent event) {
		event.enqueueWork(() -> {
			CosmeticLayer.registerRenderer(Kiwi.id("2020q3"), PlanetLayer::new);
			CosmeticLayer.registerRenderer(Kiwi.id("2020q4"), FoxTailLayer::new);
			CosmeticLayer.registerRenderer(Kiwi.id("xmas"), SantaHatLayer::new);
			CosmeticLayer.registerRenderer(Kiwi.id("sunny_milk"), SunnyMilkLayer::new);

			IEventBus eventBus = Objects.requireNonNull(ModContext.get(Kiwi.ID).modContainer.getEventBus());
			eventBus.addListener((EntityRenderersEvent.AddLayers e) -> {
				ImmutableMap.Builder<PlayerSkin.Model, CosmeticLayer> builder = ImmutableMap.builder();
				for (PlayerSkin.Model skin : e.getSkins()) {
					if (e.getSkin(skin) instanceof PlayerRenderer renderer) {
						CosmeticLayer layer = new CosmeticLayer(renderer);
						builder.put(skin, layer);
						renderer.addLayer(layer);
					}
				}
				CosmeticLayer.ALL_LAYERS = builder.build();
			});
			eventBus.addListener((RegisterRenderStateModifiersEvent e) ->
					e.registerEntityModifier(
							PlayerRenderer.class, (player, state) -> {
								state.setRenderData(CosmeticLayer.COSMETIC_KEY, CosmeticLayer.getRendererOf(player));
							}));
		});
		NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingIn e) -> ContributorsClient.changeCosmetic());
		NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut e) -> clear());
		NeoForge.EVENT_BUS.addListener((InputEvent.Key e) -> onKeyInput(Minecraft.getInstance()));
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
		CosmeticLayer.getCache().clear();
	}

	private static UUID getSelfUUID() {
		return Minecraft.getInstance().getUser().getProfileId();
	}

	private static String getSelfName() {
		return Minecraft.getInstance().getUser().getName();
	}

}