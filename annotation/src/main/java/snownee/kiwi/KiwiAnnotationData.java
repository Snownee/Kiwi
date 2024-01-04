package snownee.kiwi;

import java.util.Map;

public class KiwiAnnotationData {
	String target;
	Map<String, Object> data;

	public KiwiAnnotationData(String target, Map<String, Object> data) {
		this.target = target;
		this.data = data;
	}

	public String target() {
		return target;
	}

	public Map<String, Object> data() {
		return data == null ? Map.of() : data;
	}
}