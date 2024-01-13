package snownee.kiwi;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.resources.ResourceLocation;

public final class KiwiModules {
	private static Map<ResourceLocation, KiwiModuleContainer> MODULES = Maps.newLinkedHashMap();
	private static final Set<ResourceLocation> LOADED_MODULES = Sets.newHashSet();

	static final Set<Object> ALL_USED_REGISTRIES = Sets.newLinkedHashSet();

	private KiwiModules() {
	}

	public static void add(ResourceLocation resourceLocation, AbstractModule module, ModContext context) {
		Preconditions.checkArgument(!isLoaded(resourceLocation), "Duplicate module: %s", resourceLocation);
		LOADED_MODULES.add(resourceLocation);
		MODULES.put(resourceLocation, new KiwiModuleContainer(resourceLocation, module, context));
	}

	public static boolean isLoaded(ResourceLocation module) {
		return LOADED_MODULES.contains(module);
	}

	public static Collection<KiwiModuleContainer> get() {
		return MODULES.values();
	}

	public static KiwiModuleContainer get(ResourceLocation moduleId) {
		return MODULES.get(moduleId);
	}

	public static void clear() {
		// FabricDataGenHelper.ENABLED
		if (System.getProperty("fabric-api.datagen") != null) {
			MODULES.clear();
			MODULES = Map.of();
		}
	}

	public static void fire(Consumer<KiwiModuleContainer> consumer) {
		MODULES.values().forEach(consumer);
	}

}
