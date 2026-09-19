package snownee.kiwi.lang;

@FunctionalInterface
public interface ConditionEvaluator {
	boolean test(String expression);

	default boolean isReservedWord(String name) {
		return false;
	}
}
