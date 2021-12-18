package snownee.kiwi.contributor;

import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiClientConfig;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.config.ConfigHandler;
import snownee.kiwi.config.KiwiConfigManager;
import snownee.kiwi.contributor.client.CosmeticLayer;
import snownee.kiwi.contributor.client.gui.CosmeticScreen;
import snownee.kiwi.contributor.impl.KiwiTierProvider;
import snownee.kiwi.contributor.network.CSetCosmeticPacket;
import snownee.kiwi.contributor.network.SSyncCosmeticPacket;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.loader.event.ClientInitEvent;
import snownee.kiwi.loader.event.InitEvent;
import snownee.kiwi.util.Util;

@KiwiModule("contributors")
public class Contributors extends AbstractModule {

	public static final Map<String, ITierProvider> REWARD_PROVIDERS = Maps.newConcurrentMap();
	public static final Map<String, ResourceLocation> PLAYER_COSMETICS = Maps.newConcurrentMap();
	private static final Set<ResourceLocation> RENDERABLES = Sets.newLinkedHashSet();
	private static int DAY = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

	@Override
	protected void init(InitEvent event) {
		registerTierProvider(new KiwiTierProvider());
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			if (!(handler.player.level instanceof ServerLevel)) {
				return;
			}
			if (!(server.isSingleplayerOwner(handler.player.getGameProfile()))) {
				SSyncCosmeticPacket.send(PLAYER_COSMETICS, handler.player, false);
			}
		});
		if (!Platform.isPhysicalClient()) {
			ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
				PLAYER_COSMETICS.remove(handler.player.getGameProfile().getName());
			});
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	protected void clientInit(ClientInitEvent event) {
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
			if (entityRenderer instanceof PlayerRenderer) {
				CosmeticLayer layer = new CosmeticLayer((PlayerRenderer) entityRenderer);
				CosmeticLayer.ALL_LAYERS.add(layer);
				registrationHelper.register(layer);
			}
		});
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			changeCosmetic();
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			PLAYER_COSMETICS.clear();
			CosmeticLayer.ALL_LAYERS.forEach(l -> l.getCache().invalidateAll());
		});
		ClientTickEvents.END_CLIENT_TICK.register(this::onKeyInput);
	}

	public static boolean isContributor(String author, String playerName) {
		return REWARD_PROVIDERS.getOrDefault(author.toLowerCase(Locale.ENGLISH), ITierProvider.Empty.INSTANCE).isContributor(playerName);
	}

	public static boolean isContributor(String author, String playerName, String tier) {
		return REWARD_PROVIDERS.getOrDefault(author.toLowerCase(Locale.ENGLISH), ITierProvider.Empty.INSTANCE).isContributor(playerName, tier);
	}

	public static boolean isContributor(String author, Player player) {
		return isContributor(author, player.getGameProfile().getName());
	}

	public static boolean isContributor(String author, Player player, String tier) {
		return isContributor(author, player.getGameProfile().getName(), tier);
	}

	public static Set<ResourceLocation> getPlayerTiers(String playerName) {
		/* off */
        return REWARD_PROVIDERS.values().stream()
                .flatMap(tp -> tp.getPlayerTiers(playerName).stream()
                        .map(s -> new ResourceLocation(tp.getAuthor().toLowerCase(Locale.ENGLISH), s)))
                .collect(Collectors.toSet());
        /* on */
	}

	public static Set<ResourceLocation> getTiers() {
		/* off */
        return REWARD_PROVIDERS.values().stream()
                .flatMap(tp -> tp.getTiers().stream()
                        .map(s -> new ResourceLocation(tp.getAuthor().toLowerCase(Locale.ENGLISH), s)))
                .collect(Collectors.toSet());
        /* on */
	}

	public static void registerTierProvider(ITierProvider rewardProvider) {
		String namespace = rewardProvider.getAuthor().toLowerCase(Locale.ENGLISH);
		REWARD_PROVIDERS.put(namespace, rewardProvider);
		for (String tier : rewardProvider.getRenderableTiers()) {
			RENDERABLES.add(new ResourceLocation(namespace, tier));
		}
	}

	@Environment(EnvType.CLIENT)
	public static void changeCosmetic() {
		ResourceLocation id = Util.RL(KiwiClientConfig.contributorCosmetic);
		if (id != null && id.getPath().isEmpty()) {
			id = null;
		}
		ResourceLocation cosmetic = id;
		canPlayerUseCosmetic(getPlayerName(), cosmetic).thenAccept(bl -> {
			if (!bl) {
				ConfigHandler cfg = KiwiConfigManager.getHandler(KiwiClientConfig.class);
				KiwiClientConfig.contributorCosmetic = "";
				cfg.save();
				return;
			}
			CSetCosmeticPacket.send(cosmetic);
			if (cosmetic == null) {
				PLAYER_COSMETICS.remove(getPlayerName());
			} else {
				PLAYER_COSMETICS.put(getPlayerName(), cosmetic);
				Kiwi.logger.info("Enabled contributor effect: {}", cosmetic);
			}
			CosmeticLayer.ALL_LAYERS.forEach(l -> l.getCache().invalidate(getPlayerName()));
		});
	}

	@Environment(EnvType.CLIENT)
	public static void changeCosmetic(Map<String, ResourceLocation> changes) {
		changes.forEach((k, v) -> {
			if (v == null) {
				PLAYER_COSMETICS.remove(k);
			} else {
				PLAYER_COSMETICS.put(k, v);
			}
		});
		CosmeticLayer.ALL_LAYERS.forEach(l -> l.getCache().invalidateAll(changes.keySet()));
	}

	public static void changeCosmetic(ServerPlayer player, ResourceLocation cosmetic) {
		String playerName = player.getGameProfile().getName();
		canPlayerUseCosmetic(playerName, cosmetic).thenAccept(bl -> {
			if (bl) {
				if (cosmetic == null) {
					PLAYER_COSMETICS.remove(playerName);
				} else {
					PLAYER_COSMETICS.put(playerName, cosmetic);
				}
				SSyncCosmeticPacket.send(ImmutableMap.of(playerName, cosmetic), player, true);
			}
		});
	}

	public static boolean isRenderable(ResourceLocation id) {
		refreshRenderables();
		return RENDERABLES.contains(id);
	}

	public static Set<ResourceLocation> getRenderableTiers() {
		refreshRenderables();
		return Collections.unmodifiableSet(RENDERABLES);
	}

	private static void refreshRenderables() {
		int current = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		if (current != DAY) {
			DAY = current;
			RENDERABLES.clear();
			for (Entry<String, ITierProvider> entry : REWARD_PROVIDERS.entrySet()) {
				String namespace = entry.getKey();
				for (String tier : entry.getValue().getRenderableTiers()) {
					RENDERABLES.add(new ResourceLocation(namespace, tier));
				}
			}
		}
	}

	public static CompletableFuture<Boolean> canPlayerUseCosmetic(String playerName, ResourceLocation cosmetic) {
		if (cosmetic == null || cosmetic.getPath().isEmpty()) { // Set to empty
			return CompletableFuture.completedFuture(Boolean.TRUE);
		}
		if (!isRenderable(cosmetic)) {
			return CompletableFuture.completedFuture(Boolean.FALSE);
		}
		ITierProvider provider = REWARD_PROVIDERS.getOrDefault(cosmetic.getNamespace().toLowerCase(Locale.ENGLISH), ITierProvider.Empty.INSTANCE);
		if (!provider.isContributor(playerName, cosmetic.getPath())) {
			if (!Platform.isPhysicalClient()) {
				return provider.refresh().thenApply($ -> provider.isContributor(playerName, cosmetic.getPath()));
			} else {
				return CompletableFuture.completedFuture(Boolean.FALSE);
			}
		}
		return CompletableFuture.completedFuture(Boolean.TRUE);
	}

	@Environment(EnvType.CLIENT)
	private static String getPlayerName() {
		return Minecraft.getInstance().getUser().getName();
	}

	private int hold;

	@Environment(EnvType.CLIENT)
	public void onKeyInput(Minecraft mc) {
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
