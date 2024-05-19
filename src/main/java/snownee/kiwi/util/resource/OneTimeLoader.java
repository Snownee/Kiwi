package snownee.kiwi.util.resource;

import java.io.BufferedReader;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import snownee.kiwi.Kiwi;
import snownee.kiwi.util.KUtil;

public class OneTimeLoader {
	private static final Gson GSON = new GsonBuilder().setLenient().create();

	public static <T> Map<ResourceLocation, T> load(
			ResourceManager resourceManager,
			String directory,
			Codec<T> codec) {
		return load(resourceManager, directory, codec, $ -> true);
	}

	public static <T> Map<ResourceLocation, T> load(
			ResourceManager resourceManager,
			String directory,
			Codec<T> codec,
			Predicate<ResourceLocation> listFilter) {
		var fileToIdConverter = AlternativesFileToIdConverter.yamlOrJson(directory).setListFilter(listFilter);
		Map<ResourceLocation, T> results = Maps.newHashMap();
		for (Map.Entry<ResourceLocation, Resource> entry : fileToIdConverter.listMatchingResources(resourceManager).entrySet()) {
			DataResult<T> result = parseFile(entry.getKey(), entry.getValue(), codec);
			if (result.error().isPresent()) {
				Kiwi.LOGGER.error("Failed to parse " + entry.getKey() + ": " + result.error().get());
				continue;
			}
			ResourceLocation id = fileToIdConverter.fileToId(entry.getKey());
			results.put(id, result.result().orElseThrow());
		}
		return results;
	}

	public static <T> T loadFile(ResourceManager resourceManager, String directory, ResourceLocation id, Codec<T> codec) {
		var fileToIdConverter = AlternativesFileToIdConverter.yamlOrJson(directory);
		ResourceLocation file = fileToIdConverter.idToFile(id);
		Optional<Resource> resource = resourceManager.getResource(file);
		if (resource.isEmpty()) {
			return null;
		}
		DataResult<T> result = parseFile(file, resource.get(), codec);
		if (result.error().isPresent()) {
			Kiwi.LOGGER.error("Failed to parse " + file + ": " + result.error().get());
			return null;
		}
		return result.result().orElseThrow();
	}

	public static <T> DataResult<T> parseFile(ResourceLocation file, Resource resource, Codec<T> codec) {
		String ext = file.getPath().substring(file.getPath().length() - 5);
		try (BufferedReader reader = resource.openAsReader()) {
			DataResult<T> result;
			if (ext.equals(".json")) {
				result = codec.parse(JsonOps.INSTANCE, GSON.fromJson(reader, JsonElement.class));
			} else if (ext.equals(".yaml")) {
				result = codec.parse(JavaOps.INSTANCE, KUtil.loadYaml(reader, Object.class));
			} else {
				return DataResult.error(() -> "Unknown extension: " + ext);
			}
			return result;
		} catch (Exception e) {
			return DataResult.error(() -> "Failed to load " + file + ": " + e);
		}
	}
}