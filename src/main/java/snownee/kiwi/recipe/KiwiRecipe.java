package snownee.kiwi.recipe;

public interface KiwiRecipe {
	default void kiwi$setNoRemainders(boolean bl) {}

	default boolean kiwi$noRemainders() {
		return false;
	}
}
