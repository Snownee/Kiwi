package snownee.kiwi;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;

public class ModContext {
	public static final Map<String, ModContext> ALL_CONTEXTS = Maps.newHashMap();

	public ModContainer modContainer;
	public Supplier<?> extension;

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
		Preconditions.checkNotNull(modid, "Cannot get name of kiwi module.");
		try {
			this.modContainer = ModList.get().getModContainerById(modid).get();
			this.extension = (Supplier<?>) Kiwi.FIELD_EXTENSION.get(modContainer);
		} catch (NoSuchElementException | IllegalArgumentException | IllegalAccessException e) {
			Kiwi.logger.catching(e);
		}
	}

	public void setActiveContainer() {
		ModLoadingContext.get().setActiveContainer(modContainer, extension.get());
	}
}
