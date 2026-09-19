package snownee.kiwi.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

class DefineResolverTest {
	private static final ConditionEvaluator EVALUATOR = new ConditionEvaluator() {
		@Override
		public boolean test(String expression) {
			return true;
		}

		@Override
		public boolean isReservedWord(String name) {
			return name.equalsIgnoreCase("HAS") || name.equalsIgnoreCase("MC");
		}
	};

	private static String prepare(String expression, Map<String, String> defines) {
		return DefineResolver.prepare(expression, defines, EVALUATOR, (key, message) -> {}, "test");
	}

	@Test
	void hasShorthand() {
		assertEquals("HAS(\"jei\")", prepare("has:jei", Map.of()));
	}

	@Test
	void hasModule() {
		assertEquals("HAS(\"@xkdeco:main\")", prepare("has:@xkdeco:main", Map.of()));
	}

	@Test
	void cfgShorthand() {
		assertEquals("CFG(\"kiwi.common.eval.printExpression\")", prepare("cfg:kiwi.common.eval.printExpression", Map.of()));
	}

	@Test
	void cfgViaDefine() {
		assertEquals(
				"CFG(\"kiwi.common.eval.printExpression\")",
				prepare("p", Map.of("p", "cfg:kiwi.common.eval.printExpression")));
	}

	@Test
	void defineExpands() {
		assertEquals("HAS(\"jei\")", prepare("jei", Map.of("jei", "has:jei")));
	}

	@Test
	void recursiveDefine() {
		assertEquals("HAS(\"jei\")", prepare("a", Map.of("a", "b", "b", "has:jei")));
	}

	@Test
	void cyclicDefineBecomesFalse() {
		assertEquals("false", prepare("a", Map.of("a", "b", "b", "a")));
	}

	@Test
	void quotesAreSkipped() {
		assertEquals("'jei'", prepare("'jei'", Map.of("jei", "has:jei")));
		assertEquals("\"jei\"", prepare("\"jei\"", Map.of("jei", "has:jei")));
	}

	@Test
	void tokenBoundaryRespected() {
		assertEquals("notjei", prepare("notjei", Map.of("jei", "has:jei")));
	}

	@Test
	void reservedWordNotExpanded() {
		assertEquals("HAS", prepare("HAS", Map.of("HAS", "has:jei")));
	}

	@Test
	void combinedExpression() {
		assertEquals("HAS(\"jei\") && HAS(\"rei\")", prepare("has:jei && rei", Map.of("rei", "has:rei")));
	}

	@Test
	void cycleWarnsOnce() {
		StringBuilder messages = new StringBuilder();
		DefineResolver.prepare(
				"a",
				Map.of("a", "b", "b", "a"),
				EVALUATOR,
				(key, message) -> messages.append(message).append('\n'),
				"test");
		assertTrue(messages.toString().contains("Cyclic define 'a'"));
	}
}
