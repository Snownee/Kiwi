package snownee.kiwi.customization.duck;

public interface KPlayer {
	void kiwi$setPlaceCount(int i);

	int kiwi$getPlaceCount();

	default void kiwi$incrementPlaceCount() {
		kiwi$setPlaceCount(kiwi$getPlaceCount() + 1);
	}
}
