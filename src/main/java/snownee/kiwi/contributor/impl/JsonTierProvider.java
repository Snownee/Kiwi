package snownee.kiwi.contributor.impl;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableSetMultimap.Builder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import snownee.kiwi.Kiwi;
import snownee.kiwi.contributor.ITierProvider;
import snownee.kiwi.contributor.client.CosmeticLayer;

public class JsonTierProvider implements ITierProvider {

	private static final Gson GSON = new GsonBuilder().disableHtmlEscaping()/*.registerTypeAdapter(type, typeAdapter)*/.create();
	private final String author;
	private ImmutableSetMultimap<String, String> contributors = ImmutableSetMultimap.of();
	protected ImmutableSet<String> superusers = ImmutableSet.of();
	private final Supplier<List<String>> urlProvider;

	public JsonTierProvider(String author, Supplier<List<String>> urlProvider) {
		this.author = author;
		this.urlProvider = urlProvider;
		refresh();
	}

	public boolean load(String url) {
		try (InputStreamReader reader = new InputStreamReader(new URL(url).openStream())) {
			Map<String, Collection<String>> map = GSON.fromJson(reader, Map.class);
			if (map.containsKey("*")) {
				map.get("*").add("Dev");
				superusers = ImmutableSet.copyOf(map.get("*"));
			} else {
				superusers = ImmutableSet.of(getAuthor());
			}
			Builder<String, String> builder = ImmutableSetMultimap.builder();
			map.forEach((reward, users) -> {
				if ("*".equals(reward)) {
					return;
				}
				users.forEach(user -> builder.put(user, reward));
			});
			contributors = builder.build();
			Kiwi.LOGGER.debug("Successfully loaded {} contributors", contributors.keySet().size());
			return true;
		} catch (Exception e) {
			Kiwi.LOGGER.debug("Failed to load contributors from %s".formatted(url), e);
			return e instanceof UnknownHostException;
		}
	}

	@Override
	public CompletableFuture<Void> refresh() {
		CompletableFuture<Void> cf = new CompletableFuture<>();
		Thread thread = new Thread(() -> {
			int tried = 0;
			List<String> url = urlProvider.get();
			while (tried < url.size()) {
				if (load(url.get(tried))) {
					break;
				}
				++tried;
			}
			cf.complete(null);
		}, String.format("[Kiwi > %s] Loading Contributors", author));
		thread.setDaemon(true);
		thread.start();
		return cf;
	}

	@Override
	public String getAuthor() {
		return author;
	}

	@Override
	public Set<String> getPlayerTiers(String playerName) {
		return superusers.contains(playerName) ? getTiers() : contributors.get(playerName);
	}

	@Override
	public Set<String> getTiers() {
		return contributors.keySet();
	}

	@Override
	public List<String> getRenderableTiers() {
		return List.of();
	}

	@Environment(EnvType.CLIENT)
	@Override
	public CosmeticLayer createRenderer(
			RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> entityRenderer,
			String tier) {
		return null;
	}

}
