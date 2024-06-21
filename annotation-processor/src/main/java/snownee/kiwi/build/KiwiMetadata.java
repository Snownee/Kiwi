package snownee.kiwi.build;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import snownee.kiwi.KiwiAnnotationData;

public record KiwiMetadata(Map<String, List<KiwiAnnotationData>> map, boolean clientOnly) {

	public KiwiMetadata(boolean clientOnly) {
		this(new HashMap<>(), clientOnly);
	}

	public static KiwiMetadata of(Map<String, Object> raw) {
		Map<String, List<KiwiAnnotationData>> map = new HashMap<>();
		for (Map.Entry<String, Object> entry : raw.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value instanceof List) {
				//noinspection unchecked
				map.put(key, (List<KiwiAnnotationData>) value);
			}
		}
		return new KiwiMetadata(map, (Boolean) raw.getOrDefault("clientOnly", false));
	}

	public List<KiwiAnnotationData> get(String type) {
		return map.getOrDefault(type, List.of());
	}

}
