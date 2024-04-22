package snownee.kiwi.contributor.impl;

import java.io.InputStreamReader;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kiwi.Kiwi;
import snownee.kiwi.contributor.ITierProvider;
import snownee.kiwi.contributor.client.CosmeticLayer;

public class JsonTierProvider implements ITierProvider {
	public static final Gson GSON = new GsonBuilder().setLenient().create();
	public static final Codec<Map<String, List<String>>> CODEC = Codec.unboundedMap(Codec.STRING, Codec.STRING.listOf());

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
		try (InputStreamReader reader = new InputStreamReader(URI.create(url).toURL().openStream())) {
			JsonElement json = GSON.fromJson(reader, JsonElement.class);
			Map<String, List<String>> map = CODEC.parse(JsonOps.INSTANCE, json).result().orElseThrow();
			ImmutableSet.Builder<String> superusers = ImmutableSet.builder();
			if (map.containsKey("*")) {
				superusers.addAll(map.get("*"));
				superusers.add("Dev");
			} else {
				superusers.add(getAuthor());
			}
			ImmutableSetMultimap.Builder<String, String> contributors = ImmutableSetMultimap.builder();
			map.forEach((reward, users) -> {
				if ("*".equals(reward)) {
					return;
				}
				users.forEach(user -> contributors.put(user, reward));
			});
			this.contributors = contributors.build();
			this.superusers = superusers.build();
			Kiwi.LOGGER.debug("Successfully loaded {} contributors", this.contributors.keySet().size());
			return true;
		} catch (Exception e) {
			Kiwi.LOGGER.debug("Failed to load contributors from %s".formatted(url), e);
			return e instanceof UnknownHostException;
		}
	}

	@Override
	public CompletableFuture<Void> refresh() {
		return CompletableFuture.runAsync(() -> {
			int tried = 0;
			List<String> url = urlProvider.get();
			while (tried < url.size()) {
				if (load(url.get(tried))) {
					break;
				}
				++tried;
			}
		});
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

	@OnlyIn(Dist.CLIENT)
	@Override
	public CosmeticLayer createRenderer(
			RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> entityRenderer, String tier) {
		return null;
	}

}
