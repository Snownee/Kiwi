package snownee.kiwi.lang;

@FunctionalInterface
public interface ConditionEvaluator {
	boolean test(String expression);

	default String evaluate(String expression) {
		throw new UnsupportedOperationException("This evaluator does not support value evaluation");
	}

	default boolean isReservedWord(String name) {
		return false;
	}
}
