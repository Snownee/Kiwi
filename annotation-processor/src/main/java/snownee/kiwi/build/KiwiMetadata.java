package snownee.kiwi.build;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import snownee.kiwi.KiwiAnnotationData;

public record KiwiMetadata(Map<String, List<KiwiAnnotationData>> map) {

	public KiwiMetadata() {
		this(new TreeMap<>());
	}

	public List<KiwiAnnotationData> get(String type) {
		return map.getOrDefault(type, List.of());
	}

}
