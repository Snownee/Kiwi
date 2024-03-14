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

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.contributor.impl.KiwiTierProvider;
import snownee.kiwi.contributor.network.SSyncCosmeticPacket;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.loader.event.InitEvent;
import snownee.kiwi.network.KPacketSender;

@KiwiModule("contributors")
@KiwiModule.ClientCompanion(ContributorsClient.class)
public class Contributors extends AbstractModule {

	public static final Map<String, ITierProvider> REWARD_PROVIDERS = Maps.newConcurrentMap();
	public static final Map<String, ResourceLocation> PLAYER_COSMETICS = Maps.newConcurrentMap();
	private static final Set<ResourceLocation> RENDERABLES = Sets.newLinkedHashSet();
	private static int DAY = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

	public static boolean isContributor(String author, String playerName) {
		return REWARD_PROVIDERS.getOrDefault(author.toLowerCase(Locale.ENGLISH), ITierProvider.Empty.INSTANCE).isContributor(playerName);
	}

	public static boolean isContributor(String author, String playerName, String tier) {
		return REWARD_PROVIDERS.getOrDefault(author.toLowerCase(Locale.ENGLISH), ITierProvider.Empty.INSTANCE).isContributor(
				playerName,
				tier);
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

	public static void changeCosmetic(ServerPlayer player, ResourceLocation cosmetic) {
		String playerName = player.getGameProfile().getName();
		canPlayerUseCosmetic(playerName, cosmetic).thenAccept(bl -> {
			if (bl) {
				if (cosmetic == null) {
					PLAYER_COSMETICS.remove(playerName);
				} else {
					PLAYER_COSMETICS.put(playerName, cosmetic);
				}
				KPacketSender.sendToAllExcept(new SSyncCosmeticPacket(ImmutableMap.of(playerName, cosmetic)), player);
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
		ITierProvider provider = REWARD_PROVIDERS.getOrDefault(
				cosmetic.getNamespace().toLowerCase(Locale.ENGLISH),
				ITierProvider.Empty.INSTANCE);
		if (!provider.isContributor(playerName, cosmetic.getPath())) {
			if (!Platform.isPhysicalClient()) {
				return provider.refresh().thenApply($ -> provider.isContributor(playerName, cosmetic.getPath()));
			} else {
				return CompletableFuture.completedFuture(Boolean.FALSE);
			}
		}
		return CompletableFuture.completedFuture(Boolean.TRUE);
	}

	@Override
	protected void init(InitEvent event) {
		registerTierProvider(new KiwiTierProvider());
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			if (!(server.isSingleplayerOwner(handler.player.getGameProfile()))) {
				KPacketSender.send(new SSyncCosmeticPacket(ImmutableMap.copyOf(PLAYER_COSMETICS)), handler.player);
			}
		});
		if (!Platform.isPhysicalClient()) {
			ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
				PLAYER_COSMETICS.remove(handler.player.getGameProfile().getName());
			});
		}
	}

}
