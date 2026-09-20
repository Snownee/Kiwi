package snownee.kiwi.lang;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import snownee.kiwi.util.resource.OneTimeLoader;

public final class LangRules {
	public static final String DIRECTORY = "kiwi/langpp";
	private static final OneTimeLoader.Context CONTEXT = new OneTimeLoader.Context(new RegistryOps.RegistryInfoLookup() {
		@Override
		public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> registryKey) {
			return Optional.empty();
		}
	});
	private static final LangRules EMPTY = new LangRules(List.of());

	private final List<LangRule> rules;
	private final Map<String, Scope> scopes = new HashMap<>();

	private LangRules(List<LangRule> rules) {
		this.rules = rules;
	}

	public static LangRules load(ResourceManager resourceManager) {
		Map<Identifier, LangRule> loaded = OneTimeLoader.load(resourceManager, DIRECTORY, LangRule.CODEC, CONTEXT);
		if (loaded.isEmpty()) {
			return EMPTY;
		}
		return of(loaded.entrySet().stream()
				.sorted(Comparator.comparing(entry -> entry.getKey().toString()))
				.map(Map.Entry::getValue)
				.toList());
	}

	public static LangRules of(List<LangRule> rules) {
		return rules.isEmpty() ? EMPTY : new LangRules(List.copyOf(rules));
	}

	public boolean isEmpty() {
		return rules.isEmpty();
	}

	public @Nullable Scope scopeFor(String key) {
		List<Match> matches = null;
		for (int i = 0; i < rules.size(); i++) {
			LangRule rule = rules.get(i);
			String matched = null;
			int matchedLength = -1;
			for (String prefix : rule.prefixes()) {
				if (prefix.length() > matchedLength && key.startsWith(prefix)) {
					matched = prefix;
					matchedLength = prefix.length();
				}
			}
			if (matched != null) {
				if (matches == null) {
					matches = new ArrayList<>();
				}
				matches.add(new Match(matchedLength, i, matched));
			}
		}
		if (matches == null) {
			return null;
		}
		matches.sort(Comparator.comparingInt(Match::prefixLength).thenComparingInt(Match::index));
		List<Match> sorted = matches;
		StringBuilder signature = new StringBuilder();
		for (Match match : sorted) {
			signature.append(match.prefix).append('@').append(match.index).append('\u0001');
		}
		String signatureKey = signature.toString();
		return scopes.computeIfAbsent(signatureKey, $ -> {
			Map<String, String> defines = new HashMap<>();
			for (Match match : sorted) {
				defines.putAll(rules.get(match.index).define());
			}
			return new Scope(Map.copyOf(defines), signatureKey);
		});
	}

	private record Match(int prefixLength, int index, String prefix) {
	}

	public static final class Scope {
		private final Map<String, String> defines;
		private final String signature;
		private final Map<String, String> preparedConditions = new HashMap<>();
		private final Map<String, Boolean> conditionResults = new HashMap<>();
		private final Map<String, String> expressionResults = new HashMap<>();

		private Scope(Map<String, String> defines, String signature) {
			this.defines = defines;
			this.signature = signature;
		}

		public Predicate<String> conditionTester(ConditionEvaluator evaluator, WarningSink warnings) {
			return rawCondition -> {
				String prepared = preparedConditions.computeIfAbsent(
						rawCondition,
						$ -> DefineResolver.prepare(rawCondition, defines, evaluator, warnings, signature + '|' + rawCondition));
				return conditionResults.computeIfAbsent(prepared, evaluator::test);
			};
		}

		public Function<String, String> expressionEvaluator(ConditionEvaluator evaluator, WarningSink warnings) {
			return rawExpression -> {
				String prepared = preparedConditions.computeIfAbsent(
						rawExpression,
						$ -> DefineResolver.prepare(
								rawExpression,
								defines,
								evaluator,
								warnings,
								signature + "|eval|" + rawExpression));
				return expressionResults.computeIfAbsent(prepared, evaluator::evaluate);
			};
		}
	}
}
