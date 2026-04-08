package snownee.kiwi.contributor;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public interface ITierProvider {
	String getAuthor();

	Set<String> getTiers();

	List<String> getRenderableTiers();

	Set<String> getPlayerTiers(String playerName);

	default CompletableFuture<Void> refresh() {
		return CompletableFuture.completedFuture(null);
	}

	@OnlyIn(Dist.CLIENT)
	@Nullable
	RenderLayer<AvatarRenderState, PlayerModel> createRenderer(RenderLayerParent<AvatarRenderState, PlayerModel> entityRenderer, String tier);

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
			return Collections.EMPTY_SET;
		}

		@Override
		public Set<String> getPlayerTiers(String playerName) {
			return Collections.EMPTY_SET;
		}

		@Override
		public List<String> getRenderableTiers() {
			return Collections.EMPTY_LIST;
		}

		@OnlyIn(Dist.CLIENT)
		@Override
		public @Nullable RenderLayer<AvatarRenderState, PlayerModel> createRenderer(
				RenderLayerParent<AvatarRenderState, PlayerModel> entityRenderer,
				String tier) {
			return null;
		}

	}
}
