package snownee.kiwi.util.resource;

import java.io.BufferedReader;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.ezylang.evalex.Expression;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import snownee.kiwi.Kiwi;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.util.KEval;
import snownee.kiwi.util.Util;
import snownee.kiwi.util.codec.JavaOps;

public class OneTimeLoader {
	private static final Gson GSON = new GsonBuilder().setLenient().create();

	public static <T> Map<ResourceLocation, T> load(ResourceManager resourceManager, String directory, Codec<T> codec, Context context) {
		var fileToIdConverter = AlternativesFileToIdConverter.yamlOrJson(directory);
		Map<ResourceLocation, T> results = Maps.newHashMap();
		for (Map.Entry<ResourceLocation, Resource> entry : fileToIdConverter.listMatchingResources(resourceManager).entrySet()) {
			ResourceLocation key = entry.getKey();
			if (context.isNamespaceDisabled(key.getNamespace())) {
				continue;
			}
			DataResult<T> result = parseFile(key, entry.getValue(), codec, context);
			if (result == null) {
				continue;
			}
			if (result.error().isPresent()) {
				Kiwi.LOGGER.error("Failed to parse " + key + ": " + result.error().get());
				continue;
			}
			ResourceLocation id = fileToIdConverter.fileToId(key);
			results.put(id, result.result().orElseThrow());
		}
		return results;
	}

	public static <T> @Nullable T loadFile(
			ResourceManager resourceManager,
			String directory,
			ResourceLocation id,
			Codec<T> codec,
			@Nullable Context context) {
		var fileToIdConverter = AlternativesFileToIdConverter.yamlOrJson(directory);
		ResourceLocation file = fileToIdConverter.idToFile(id);
		Optional<Resource> resource = resourceManager.getResource(file);
		if (resource.isEmpty()) {
			return null;
		}
		DataResult<T> result = parseFile(file, resource.get(), codec, context);
		if (result == null) {
			return null;
		}
		if (result.error().isPresent()) {
			Kiwi.LOGGER.error("Failed to parse " + file + ": " + result.error().get());
			return null;
		}
		return result.result().orElseThrow();
	}

	public static <T> @Nullable DataResult<T> parseFile(
			ResourceLocation file,
			Resource resource,
			Codec<T> codec,
			@Nullable Context context) {
		String ext = file.getPath().substring(file.getPath().length() - 5);
		try (BufferedReader reader = resource.openAsReader()) {
			Dynamic<?> dynamic;
			if (ext.equals(".json")) {
				JsonElement value = GSON.fromJson(reader, JsonElement.class);
				dynamic = new Dynamic<>(JsonOps.INSTANCE, value);
			} else if (ext.equals(".yaml")) {
				Object value = Util.loadYaml(reader, Object.class);
				dynamic = new Dynamic<>(JavaOps.INSTANCE, value);
			} else {
				return DataResult.error(() -> "Unknown extension: " + ext);
			}
			if (context != null) {
				Optional<String> condition = dynamic.get("condition").asString().result();
				if (condition.isPresent()) {
					try {
						Expression expression = context.getExpression(condition.get());
						if (expression.evaluate().getBooleanValue() != Boolean.FALSE) {
							return null;
						}
					} catch (Exception e) {
						Kiwi.LOGGER.error("Failed to parse condition in " + file + ": " + e);
					}
				}
			}
			return codec.parse(dynamic);
		} catch (Exception e) {
			return DataResult.error(() -> "Failed to load " + file + ": " + e);
		}
	}

	public static class Context {
		private Map<String, Expression> cachedExpressions;
		private Set<String> disabledNamespaces;
		private MappingResolver mappingResolver;

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

		public MappingResolver mappingResolver() {
			if (mappingResolver == null) {
				mappingResolver = Platform.newMappingResolver();
			}
			return mappingResolver;
		}
	}
}
