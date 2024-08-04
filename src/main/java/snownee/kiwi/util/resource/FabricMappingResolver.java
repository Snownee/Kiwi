package snownee.kiwi.util.resource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Objects;

import com.google.gson.Gson;

import snownee.kiwi.loader.Platform;

public class FabricMappingResolver implements MappingResolver {
	private final Map<String, String> data;

	public static FabricMappingResolver create() {
		try (
				InputStream is = FabricMappingResolver.class.getResourceAsStream("/mapping.kiwi");
				InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(is))) {
			//noinspection unchecked
			return new FabricMappingResolver((Map<String, String>) new Gson().fromJson(isr, Map.class));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private FabricMappingResolver(Map<String, String> data) {
		this.data = data;
	}

	@Override
	public String unmapClass(String clazz) {
		if (Platform.isProduction() && clazz.startsWith("net.minecraft.")) {
			clazz = clazz.substring("net.minecraft.".length());
			return "net.minecraft." + data.getOrDefault(clazz, clazz);
		}
		return clazz;
	}
}
