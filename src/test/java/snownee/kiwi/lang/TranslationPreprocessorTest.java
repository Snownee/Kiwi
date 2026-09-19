package snownee.kiwi.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class TranslationPreprocessorTest {
	private static final ConditionEvaluator EVALUATOR = new ConditionEvaluator() {
		@Override
		public boolean test(String expression) {
			return expression.contains("\"rei\"");
		}

		@Override
		public boolean isReservedWord(String name) {
			return name.equalsIgnoreCase("HAS");
		}
	};
	private static final WarningSink NOOP = (key, message) -> {};

	private static final LangRules RULES = LangRules.of(List.of(
			new LangRule(List.of("tip."), Map.of("jei", "has:jei")),
			new LangRule(List.of("tip.sub."), Map.of("jei", "has:rei"))));

	private static Map<String, String> process(Map<String, String> translations, LangRules rules) {
		return TranslationPreprocessor.process(translations, rules, EVALUATOR, NOOP);
	}

	@Test
	void matchingPrefixProcessed() {
		Map<String, String> result = process(
				Map.of("tip.viewer", "<#if jei>with<#else>without<#endif>"),
				RULES);
		assertEquals(Map.of("tip.viewer", "without"), result);
	}

	@Test
	void nonMatchingPrefixUntouched() {
		Map<String, String> input = Map.of("other.viewer", "<#if jei>with<#endif>");
		assertEquals(input, process(input, RULES));
	}

	@Test
	void noDirectiveUntouched() {
		Map<String, String> input = Map.of("tip.plain", "nothing here");
		assertEquals(input, process(input, RULES));
	}

	@Test
	void longestPrefixOverrides() {
		Map<String, String> result = process(
				Map.of("tip.sub.viewer", "<#if jei>with<#else>without<#endif>"),
				RULES);
		assertEquals(Map.of("tip.sub.viewer", "with"), result);
	}

	@Test
	void definesMergedAcrossPrefixes() {
		LangRules rules = LangRules.of(List.of(
				new LangRule(List.of("m."), Map.of("a", "has:jei")),
				new LangRule(List.of("m.sub."), Map.of("b", "has:rei"))));
		Map<String, String> result = process(Map.of("m.sub.x", "<#if b>R<#endif>"), rules);
		assertEquals(Map.of("m.sub.x", "R"), result);
	}

	@Test
	void falseWithoutElseYieldsEmptyValue() {
		Map<String, String> result = process(Map.of("tip.x", "<#if jei>y<#endif>"), RULES);
		assertEquals(Map.of("tip.x", ""), result);
	}

	@Test
	void malformedKeepsOriginalAndWarns() {
		List<String> warnings = new ArrayList<>();
		Map<String, String> input = Map.of("tip.bad", "<#if jei>y");
		Map<String, String> result = TranslationPreprocessor.process(input, RULES, EVALUATOR, (key, message) -> warnings.add(message));
		assertEquals(input, result);
		assertEquals(1, warnings.size());
	}

	@Test
	void evaluationErrorKeepsOriginal() {
		ConditionEvaluator throwing = new ConditionEvaluator() {
			@Override
			public boolean test(String expression) {
				throw new IllegalStateException("boom");
			}

			@Override
			public boolean isReservedWord(String name) {
				return name.equalsIgnoreCase("HAS");
			}
		};
		Map<String, String> input = Map.of("tip.x", "<#if jei>y<#endif>");
		LangRules rules = LangRules.of(List.of(new LangRule(List.of("tip."), Map.of("jei", "has:jei"))));
		Map<String, String> result = TranslationPreprocessor.process(input, rules, throwing, NOOP);
		assertEquals(input, result);
	}

	@Test
	void unchangedReturnsSameInstance() {
		Map<String, String> input = Map.of("tip.plain", "nothing here");
		assertTrue(process(input, RULES) == input);
	}
}
