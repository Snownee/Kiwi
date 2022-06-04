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

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiClientConfig;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.config.ConfigHandler;
import snownee.kiwi.config.KiwiConfigManager;
import snownee.kiwi.contributor.client.CosmeticLayer;
import snownee.kiwi.contributor.impl.KiwiTierProvider;
import snownee.kiwi.contributor.network.CSetCosmeticPacket;
import snownee.kiwi.contributor.network.SSyncCosmeticPacket;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.loader.event.InitEvent;
import snownee.kiwi.util.Util;

@KiwiModule("contributors")
@KiwiModule.Subscriber
public class Contributors extends AbstractModule {

	public static final Map<String, ITierProvider> REWARD_PROVIDERS = Maps.newConcurrentMap();
	public static final Map<String, ResourceLocation> PLAYER_COSMETICS = Maps.newConcurrentMap();
	private static final Set<ResourceLocation> RENDERABLES = Sets.newLinkedHashSet();
	private static int DAY = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

	@Override
	protected void preInit() {
		if (Platform.isPhysicalClient()) {
			FMLJavaModLoadingContext.get().getModEventBus().addListener(ContributorsClient::addLayers);
			MinecraftForge.EVENT_BUS.register(ContributorsClient.class);
		}
	}

	@Override
	protected void init(InitEvent event) {
		registerTierProvider(new KiwiTierProvider());
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

	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (!(event.getEntity().level instanceof ServerLevel)) {
			return;
		}
		Player player = event.getPlayer();
		if (!((ServerLevel) event.getEntity().level).getServer().isSingleplayerOwner(player.getGameProfile())) {
			SSyncCosmeticPacket.send(PLAYER_COSMETICS, (ServerPlayer) player, false);
		}
	}

	@OnlyIn(Dist.DEDICATED_SERVER)
	@SubscribeEvent
	public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		PLAYER_COSMETICS.remove(event.getPlayer().getGameProfile().getName());
	}

	@OnlyIn(Dist.CLIENT)
	public static void changeCosmetic() {
		ResourceLocation id = Util.RL(KiwiClientConfig.contributorCosmetic);
		if (id != null && id.getPath().isEmpty()) {
			id = null;
		}
		ResourceLocation cosmetic = id;
		canPlayerUseCosmetic(getPlayerName(), cosmetic).thenAccept(bl -> {
			if (!bl) {
				ConfigHandler cfg = KiwiConfigManager.getHandler(KiwiClientConfig.class);
				ConfigValue<String> val = (ConfigValue<String>) cfg.getValueByPath("contributorEffect");
				val.set("");
				cfg.refresh();
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

	@OnlyIn(Dist.CLIENT)
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

	@OnlyIn(Dist.CLIENT)
	private static String getPlayerName() {
		return Minecraft.getInstance().getUser().getName();
	}

}
