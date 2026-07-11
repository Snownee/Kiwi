package snownee.kiwi.util.resource;

import java.io.BufferedReader;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.ezylang.evalex.Expression;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.Strictness;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;

import net.minecraft.resources.Identifier;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.flag.FeatureFlagSet;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import snownee.kiwi.Kiwi;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.util.KEval;
import snownee.kiwi.util.KUtil;

public class OneTimeLoader {
	private static final Gson GSON = new GsonBuilder().setStrictness(Strictness.LENIENT).create();

	public static <T> Map<Identifier, T> load(ResourceManager resourceManager, String directory, Codec<T> codec, Context context) {
		return load(resourceManager, AlternativesFileToIdConverter.yamlOrJson(directory), codec, context);
	}

	public static <T> Map<Identifier, T> load(
			ResourceManager resourceManager,
			AlternativesFileToIdConverter lister,
			Codec<T> codec,
			Context context) {
		Map<Identifier, T> results = Maps.newHashMap();
		for (Map.Entry<Identifier, Resource> entry : lister.listMatchingResources(resourceManager).entrySet()) {
			Identifier key = entry.getKey();
			if (context.isNamespaceDisabled(key.getNamespace())) {
				continue;
			}
			DataResult<T> result = parseFile(key, entry.getValue(), codec, context);
			if (result == null) {
				continue;
			}
			if (result.error().isPresent()) {
				Kiwi.LOGGER.error("Failed to parse {}: {}", key, result.error().get());
				continue;
			}
			Identifier id = lister.fileToId(key);
			results.put(id, result.result().orElseThrow());
		}
		return results;
	}

	public static <T> @Nullable T loadFile(
			ResourceManager resourceManager,
			String directory,
			Identifier id,
			Codec<T> codec,
			Context context) {
		var fileToIdConverter = AlternativesFileToIdConverter.yamlOrJson(directory);
		Identifier file = fileToIdConverter.idToFile(id);
		Optional<Resource> resource = resourceManager.getResource(file);
		if (resource.isEmpty()) {
			return null;
		}
		DataResult<T> result = parseFile(file, resource.get(), codec, context);
		if (result == null) {
			return null;
		}
		if (result.error().isPresent()) {
			Kiwi.LOGGER.error("Failed to parse {}: {}", file, result.error().get());
			return null;
		}
		return result.result().orElseThrow();
	}

	public static <T> @Nullable DataResult<T> parseFile(
			Identifier file,
			Resource resource,
			Codec<T> codec,
			Context context) {
		String ext = file.getPath().substring(file.getPath().length() - 5);
		try (BufferedReader reader = resource.openAsReader()) {
			Dynamic<?> dynamic;
			if (ext.equals(".json")) {
				JsonElement value = GSON.fromJson(reader, JsonElement.class);
				dynamic = new Dynamic<>(JsonOps.INSTANCE, value);
			} else if (ext.equals(".yaml")) {
				Object value = KUtil.loadYaml(reader, Object.class);
				dynamic = new Dynamic<>(JavaOps.INSTANCE, value);
			} else {
				return DataResult.error(() -> "Unknown extension: " + ext);
			}
			DataResult<Platform.ConditionDecision> nativeConditions = Platform.applyResourceConditions(
					file, dynamic, context.registryProvider, context.conditionContext, context.stage);
			if (nativeConditions.error().isPresent()) {
				return DataResult.error(() -> nativeConditions.error().orElseThrow().message());
			}
			if (nativeConditions.result().orElseThrow() == Platform.ConditionDecision.SKIP) {
				return null;
			}
			Optional<? extends Dynamic<?>> conditionValue = dynamic.get("kiwi:condition").result();
			if (conditionValue.isEmpty()) {
				conditionValue = dynamic.get("condition").result();
			}
			Optional<String> condition = Optional.empty();
			if (conditionValue.isPresent()) {
				DataResult<String> conditionResult = conditionValue.get().asString();
				if (conditionResult.error().isPresent()) {
					return DataResult.error(() -> "Failed to parse condition in " + file + ": " + conditionResult.error().orElseThrow().message());
				}
				condition = conditionResult.result();
			}
			if (condition.isPresent()) {
				try {
					Expression expression = context.getExpression(condition.get());
					if (expression.evaluate().getBooleanValue() != Boolean.FALSE) {
						return null;
					}
				} catch (Exception e) {
					return DataResult.error(() -> "Failed to parse condition in " + file + ": " + e);
				}
			}
			return codec.parse(dynamic);
		} catch (Exception e) {
			return DataResult.error(() -> "Failed to load " + file + ": " + e);
		}
	}

	public static class Context {
		private final HolderLookup.Provider registryProvider;
		private final ICondition.@Nullable IContext conditionContext;
		private final String stage;
		private @Nullable Map<String, Expression> cachedExpressions;
		private @Nullable Set<String> disabledNamespaces;

		private Context(HolderLookup.Provider registryProvider, ICondition.@Nullable IContext conditionContext, String stage) {
			this.registryProvider = registryProvider;
			this.conditionContext = conditionContext;
			this.stage = stage;
		}

		public static Context unavailable(HolderLookup.Provider registryProvider, String stage) {
			return new Context(registryProvider, null, stage);
		}

		public static Context runtime(HolderLookup.Provider registryProvider, FeatureFlagSet enabledFeatures, String stage) {
			return new Context(registryProvider, Platform.conditionContext(registryProvider, enabledFeatures), stage);
		}

		public Expression getExpression(String expression) {
			if (cachedExpressions == null) {
				cachedExpressions = Maps.newHashMap();
			}
			return cachedExpressions.computeIfAbsent(expression, $ -> new Expression($, KEval.config()));
		}

		public void addDisabledNamespace(String namespace) {
			if (disabledNamespaces == null) {
				disabledNamespaces = Sets.newHashSet();
			}
			disabledNamespaces.add(namespace);
		}

		public boolean isNamespaceDisabled(String namespace) {
			return disabledNamespaces != null && disabledNamespaces.contains(namespace);
		}
	}
}
