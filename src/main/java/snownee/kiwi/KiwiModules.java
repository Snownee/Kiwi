package snownee.kiwi;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import snownee.kiwi.loader.Platform;

public final class KiwiModules {
	private static Map<Identifier, KiwiModuleContainer> MODULES = Maps.newLinkedHashMap();
	private static final Set<Identifier> LOADED_MODULES = Sets.newHashSet();

	static final Set<ResourceKey<? extends Registry<?>>> ALL_USED_REGISTRIES = Sets.newLinkedHashSet();

	private KiwiModules() {
	}

	public static void add(Identifier identifier, AbstractModule module, ModContext context) {
		Preconditions.checkArgument(!isLoaded(identifier), "Duplicate module: %s", identifier);
		LOADED_MODULES.add(identifier);
		MODULES.put(identifier, new KiwiModuleContainer(identifier, module, context));
	}

	public static boolean isLoaded(Identifier module) {
		return LOADED_MODULES.contains(module);
	}

	public static Collection<KiwiModuleContainer> get() {
		return MODULES.values();
	}

	public static KiwiModuleContainer get(Identifier moduleId) {
		return MODULES.get(moduleId);
	}

	public static void clear() {
		if (!Platform.isDataGen()) {
			MODULES = Map.of();
		}
	}

	public static void fire(Consumer<KiwiModuleContainer> consumer) {
		MODULES.values().forEach(consumer);
	}

}
