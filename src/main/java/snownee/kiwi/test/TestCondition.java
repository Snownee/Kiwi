package snownee.kiwi.test;

import snownee.kiwi.KiwiModule.LoadingCondition;
import snownee.kiwi.LoadingContext;

public class TestCondition {

	@LoadingCondition("test")
	public static boolean test(LoadingContext ctx) {
		return true;
	}
}
