package snownee.kiwi.contributor;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kiwi.contributor.client.RewardLayer;

public interface ITierProvider {
	String getAuthor();

	Set<String> getTiers();

	List<String> getRenderableTiers();

	Set<String> getPlayerTiers(String playerName);

	default CompletableFuture<Void> refresh() {
		return CompletableFuture.completedFuture(null);
	}

	@OnlyIn(Dist.CLIENT)
	RewardLayer createRenderer(IEntityRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> entityRenderer, String tier);

	default boolean isContributor(String playerName) {
		return !getPlayerTiers(playerName).isEmpty();
	}

	default boolean isContributor(String playerName, String tier) {
		return getPlayerTiers(playerName).contains(tier);
	}

	public static enum Empty implements ITierProvider {
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
		public RewardLayer createRenderer(IEntityRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> entityRenderer, String tier) {
			return null;
		}

	}
}
