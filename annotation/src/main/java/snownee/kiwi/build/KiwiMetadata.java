package snownee.kiwi.build;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import snownee.kiwi.KiwiAnnotationData;

public record KiwiMetadata(Map<String, List<KiwiAnnotationData>> map, boolean useDataModule) {

	public KiwiMetadata(boolean useDataModule) {
		this(new TreeMap<>(), useDataModule);
	}

	public static KiwiMetadata of(Map<String, Object> raw) {
		Map<String, List<KiwiAnnotationData>> map = new TreeMap<>();
		for (Map.Entry<String, Object> entry : raw.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value instanceof List) {
				//noinspection unchecked
				map.put(key, (List<KiwiAnnotationData>) value);
			}
		}
		return new KiwiMetadata(map, (Boolean) raw.getOrDefault("useDataModule", false));
	}

	public List<KiwiAnnotationData> get(String type) {
		return map.getOrDefault(type, List.of());
	}

	public Map<String, Object> dump() {
		Map<String, Object> result = new TreeMap<>();
		for (Map.Entry<String, List<KiwiAnnotationData>> entry : map.entrySet()) {
			result.put(entry.getKey(), entry.getValue().stream().map(KiwiAnnotationData::dump).toList());
		}
		if (useDataModule) {
			result.put("useDataModule", true);
		}
		return result;
	}

}
