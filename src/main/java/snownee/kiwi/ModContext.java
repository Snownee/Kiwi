package snownee.kiwi;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import com.google.common.collect.Maps;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;

public class ModContext {
	public static final Map<String, ModContext> ALL_CONTEXTS = Maps.newHashMap();

	public ModContainer modContainer;

	public static ModContext get(String modid) {
		return ALL_CONTEXTS.computeIfAbsent(modid, ModContext::new);
	}

	private ModContext(String modid) {
		Objects.requireNonNull(modid, "Cannot get name of kiwi module.");
		try {
			modContainer = ModList.get().getModContainerById(modid).orElseThrow();
		} catch (NoSuchElementException e) {
			Kiwi.LOGGER.error("Cannot find mod container for modid {}", modid);
		}
	}

	public void setActiveContainer() {
		ModLoadingContext.get().setActiveContainer(modContainer);
	}
}
