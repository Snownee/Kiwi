package snownee.kiwi;

import java.util.Map;

public class KiwiAnnotationData {
	private String target;
	private Map<String, Object> data;

	public void setTarget(String target) {
		this.target = target;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	public String getTarget() {
		return target;
	}

	public Map<String, Object> getData() {
		return data == null ? Map.of() : data;
	}
}
