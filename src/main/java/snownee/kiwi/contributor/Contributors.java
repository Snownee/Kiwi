package snownee.kiwi.contributor;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
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
	public static final Map<UUID, Identifier> PLAYER_COSMETICS = Maps.newConcurrentMap();
	private static final Set<Identifier> COSMETIC_IDS = Sets.newLinkedHashSet();
	private static int DAY = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

	public static boolean isContributor(String author, String playerName) {
		if (!Platform.isProduction()) {
			return true;
		}
		return REWARD_PROVIDERS.getOrDefault(author.toLowerCase(Locale.ENGLISH), ITierProvider.Empty.INSTANCE).isContributor(playerName);
	}

	public static boolean isContributor(String author, String playerName, String tier) {
		if (!Platform.isProduction()) {
			return true;
		}
		return REWARD_PROVIDERS.getOrDefault(author.toLowerCase(Locale.ENGLISH), ITierProvider.Empty.INSTANCE).isContributor(
				playerName,
				tier);
	}

	public static boolean isContributor(String author, Player player) {
		return isContributor(author, player.getGameProfile().name());
	}

	public static boolean isContributor(String author, Player player, String tier) {
		return isContributor(author, player.getGameProfile().name(), tier);
	}

	public static Set<Identifier> getPlayerTiers(String playerName) {
		/* off */
		return REWARD_PROVIDERS.values().stream()
				.flatMap(tp -> tp.getPlayerTiers(playerName).stream()
						.map(s -> Identifier.fromNamespaceAndPath(tp.getAuthor().toLowerCase(Locale.ENGLISH), s)))
				.collect(Collectors.toSet());
		/* on */
	}

	public static Set<Identifier> getTiers() {
		/* off */
		return REWARD_PROVIDERS.values().stream()
				.flatMap(tp -> tp.getTiers().stream()
						.map(s -> Identifier.fromNamespaceAndPath(tp.getAuthor().toLowerCase(Locale.ENGLISH), s)))
				.collect(Collectors.toSet());
		/* on */
	}

	public static void registerTierProvider(ITierProvider rewardProvider) {
		String namespace = rewardProvider.getAuthor().toLowerCase(Locale.ENGLISH);
		REWARD_PROVIDERS.put(namespace, rewardProvider);
		for (String tier : rewardProvider.getRenderableTiers()) {
			COSMETIC_IDS.add(Identifier.fromNamespaceAndPath(namespace, tier));
		}
	}

	public static void changeCosmetic(ServerPlayer player, @Nullable Identifier cosmetic) {
		canPlayerUseCosmetic(player.getGameProfile().name(), cosmetic).thenAccept(bl -> {
			if (bl) {
				UUID uuid = player.getUUID();
				SSyncCosmeticPacket packet;
				if (cosmetic == null) {
					PLAYER_COSMETICS.remove(uuid);
					packet = new SSyncCosmeticPacket(Map.of(), List.of(uuid));
				} else {
					PLAYER_COSMETICS.put(uuid, cosmetic);
					packet = new SSyncCosmeticPacket(Map.of(uuid, cosmetic), List.of());
				}
				KPacketSender.sendToAll(packet, player.level().getServer());
			}
		});
	}

	public static boolean isRenderable(Identifier id) {
		refreshRenderables();
		return COSMETIC_IDS.contains(id);
	}

	public static Set<Identifier> getRenderableTiers() {
		refreshRenderables();
		return Collections.unmodifiableSet(COSMETIC_IDS);
	}

	private static void refreshRenderables() {
		int current = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		if (current != DAY) {
			DAY = current;
			COSMETIC_IDS.clear();
			for (Entry<String, ITierProvider> entry : REWARD_PROVIDERS.entrySet()) {
				String namespace = entry.getKey();
				for (String tier : entry.getValue().getRenderableTiers()) {
					COSMETIC_IDS.add(Identifier.fromNamespaceAndPath(namespace, tier));
				}
			}
		}
	}

	public static CompletableFuture<Boolean> canPlayerUseCosmetic(String playerName, @Nullable Identifier cosmetic) {
		if (cosmetic == null || cosmetic.getPath().isEmpty()) { // Set to empty
			return CompletableFuture.completedFuture(Boolean.TRUE);
		}
		if (!isRenderable(cosmetic)) {
			return CompletableFuture.completedFuture(Boolean.FALSE);
		}
		ITierProvider provider = REWARD_PROVIDERS.getOrDefault(
				cosmetic.getNamespace().toLowerCase(Locale.ENGLISH),
				ITierProvider.Empty.INSTANCE);
		if (!isContributor(playerName, cosmetic.getPath())) {
			if (!Platform.isPhysicalClient()) {
				return provider.refresh().thenApply($ -> isContributor(playerName, cosmetic.getPath()));
			} else {
				return CompletableFuture.completedFuture(Boolean.FALSE);
			}
		}
		return CompletableFuture.completedFuture(Boolean.TRUE);
	}

	@Override
	protected void init(InitEvent event) {
		registerTierProvider(new KiwiTierProvider());
		NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent e) -> {
			Player player = e.getEntity();
			if (player.level().getServer() != null && !player.level().getServer().isSingleplayerOwner(player.nameAndId())) {
				KPacketSender.send(new SSyncCosmeticPacket(Map.copyOf(PLAYER_COSMETICS), List.of()), player);
			}
		});
		if (!Platform.isPhysicalClient()) {
			NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent e) -> {
				PLAYER_COSMETICS.remove(e.getEntity().getGameProfile().name());
			});
		}
	}
}