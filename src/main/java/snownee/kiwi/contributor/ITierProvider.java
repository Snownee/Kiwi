package snownee.kiwi.contributor;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import snownee.kiwi.contributor.client.CosmeticLayer;

public interface ITierProvider {
	String getAuthor();

	Set<String> getTiers();

	List<String> getRenderableTiers();

	Set<String> getPlayerTiers(String playerName);

	default CompletableFuture<Void> refresh() {
		return CompletableFuture.completedFuture(null);
	}

	@Environment(EnvType.CLIENT)
	CosmeticLayer createRenderer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> entityRenderer, String tier);

	default boolean isContributor(String playerName) {
		return !getPlayerTiers(playerName).isEmpty();
	}

	default boolean isContributor(String playerName, String tier) {
		return getPlayerTiers(playerName).contains(tier);
	}

	public enum Empty implements ITierProvider {
		INSTANCE;

		@Override
		public String getAuthor() {
			return "";
		}

		@Override
		public Set<String> getTiers() {
			return Set.of();
		}

		@Override
		public Set<String> getPlayerTiers(String playerName) {
			return Set.of();
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
}
