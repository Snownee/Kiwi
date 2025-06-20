package snownee.kiwi.contributor;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface ITierProvider {
	String getAuthor();

	Set<String> getTiers();

	List<String> getRenderableTiers();

	Set<String> getPlayerTiers(String playerName);

	default CompletableFuture<Void> refresh() {
		return CompletableFuture.completedFuture(null);
	}

	default boolean isContributor(String playerName) {
		return !getPlayerTiers(playerName).isEmpty();
	}

	default boolean isContributor(String playerName, String tier) {
		return getPlayerTiers(playerName).contains(tier);
	}

	enum Empty implements ITierProvider {
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
	}
}
