package snownee.kiwi;

import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Maps;

public class ModContext {
	public static final Map<String, ModContext> ALL_CONTEXTS = Maps.newHashMap();

	//	public ModContainer modContainer;

	public static ModContext get(String modid) {
		if (ALL_CONTEXTS.containsKey(modid)) {
			return ALL_CONTEXTS.get(modid);
		} else {
			ModContext context = new ModContext(modid);
			ALL_CONTEXTS.put(modid, context);
			return context;
		}
	}

	private ModContext(String modid) {
		Objects.requireNonNull(modid, "Cannot get name of kiwi module.");
		//		try {
		//			modContainer = ModList.get().getModContainerById(modid).get();
		//		} catch (NoSuchElementException e) {
		//			Kiwi.logger.catching(e);
		//		}
	}

	public void setActiveContainer() {
		//		ModLoadingContext.get().setActiveContainer(modContainer);
	}
}
