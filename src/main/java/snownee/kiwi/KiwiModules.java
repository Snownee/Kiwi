package snownee.kiwi;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.resources.ResourceLocation;

public final class KiwiModules {
	private static Map<ResourceLocation, ModuleInfo> MODULES = Maps.newLinkedHashMap();
	private static final Set<ResourceLocation> LOADED_MODULES = Sets.newHashSet();

	static final Set<Object> ALL_USED_REGISTRIES = Sets.newLinkedHashSet();

	private KiwiModules() {
	}

	public static void add(ResourceLocation resourceLocation, AbstractModule module, ModContext context) {
		Preconditions.checkArgument(!isLoaded(resourceLocation), "Duplicate module: %s", resourceLocation);
		LOADED_MODULES.add(resourceLocation);
		MODULES.put(resourceLocation, new ModuleInfo(resourceLocation, module, context));
	}

	//	public static void handleRegister(RegistryEvent.Register<?> event) {
	//		MODULES.values().forEach(info -> info.handleRegister(event));
	//		ModLoadingContext.get().setActiveContainer(null);
	//	}

	public static boolean isLoaded(ResourceLocation module) {
		return LOADED_MODULES.contains(module);
	}

	public static Collection<ModuleInfo> get() {
		return MODULES.values();
	}

	public static ModuleInfo get(ResourceLocation moduleId) {
		return MODULES.get(moduleId);
	}

	public static void clear() {
		// FabricDataGenHelper.ENABLED
		if (System.getProperty("fabric-api.datagen") != null) {
			MODULES.clear();
			MODULES = Map.of();
		}
	}

	public static void fire(Consumer<ModuleInfo> consumer) {
		MODULES.values().forEach(consumer);
	}

}
