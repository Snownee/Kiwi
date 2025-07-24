package snownee.kiwi;

import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Maps;

public class ModContext {
	public static final Map<String, ModContext> ALL_CONTEXTS = Maps.newHashMap();

	public static ModContext get(String modid) {
		return ALL_CONTEXTS.computeIfAbsent(modid, ModContext::new);
	}

	private ModContext(String modid) {
		Objects.requireNonNull(modid, "Cannot get name of kiwi module.");
	}

	public void setActiveContainer() {
	}
}
