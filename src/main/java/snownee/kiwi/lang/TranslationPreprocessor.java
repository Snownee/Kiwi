package snownee.kiwi.lang;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.server.packs.resources.ResourceManager;
import snownee.kiwi.Kiwi;

public final class TranslationPreprocessor {
	private static final String DIRECTIVE = "<#";

	private TranslationPreprocessor() {
	}

	public static Map<String, String> process(Map<String, String> translations, ResourceManager resourceManager) {
		return process(translations, LangRules.load(resourceManager), KEvalConditionEvaluator.INSTANCE, defaultWarningSink());
	}

	public static Map<String, String> process(
			Map<String, String> translations,
			LangRules rules,
			ConditionEvaluator evaluator,
			WarningSink warnings) {
		if (rules.isEmpty() || translations.isEmpty()) {
			return translations;
		}
		Map<String, Template> templates = new HashMap<>();
		Map<String, String> result = null;
		for (Map.Entry<String, String> entry : translations.entrySet()) {
			String value = entry.getValue();
			if (!value.contains(DIRECTIVE)) {
				continue;
			}
			LangRules.Scope scope = rules.scopeFor(entry.getKey());
			if (scope == null) {
				continue;
			}
			try {
				Template template = templates.computeIfAbsent(value, Template::parse);
				String rendered = template.render(
						scope.conditionTester(evaluator, warnings),
						scope.expressionEvaluator(evaluator, warnings));
				if (!rendered.equals(value)) {
					if (result == null) {
						result = new HashMap<>(translations);
					}
					result.put(entry.getKey(), rendered);
				}
			} catch (Exception e) {
				warnings.warn(
						"key:" + entry.getKey(),
						"Failed to preprocess translation '" + entry.getKey() + "': " + e.getMessage());
			}
		}
		return result == null ? translations : Map.copyOf(result);
	}

	private static WarningSink defaultWarningSink() {
		Set<String> seen = ConcurrentHashMap.newKeySet();
		return (key, message) -> {
			if (seen.add(key)) {
				Kiwi.LOGGER.warn("[langpp] {}", message);
			} else {
				Kiwi.LOGGER.debug("[langpp] {}", message);
			}
		};
	}
}
