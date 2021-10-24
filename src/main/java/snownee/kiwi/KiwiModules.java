package snownee.kiwi;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.CrashReportExtender;
import net.minecraftforge.fml.DatagenModLoader;
import net.minecraftforge.fml.ModLoadingContext;

public final class KiwiModules {
	private static Map<ResourceLocation, ModuleInfo> MODULES = Maps.newLinkedHashMap();
	private static final Set<ResourceLocation> LOADED_MODULES = Sets.newHashSet();

	static {
		CrashReportExtender.registerCrashCallable("Kiwi Modules", () -> {
			return "\n" + MODULES.keySet().stream().map(ResourceLocation::toString).sorted(StringUtils::compare).collect(Collectors.joining("\n\t\t", "\t\t", ""));
		});
	}

	private KiwiModules() {
	}

	public static void addInstance(ResourceLocation resourceLocation, AbstractModule module, ModContext context) {
		Preconditions.checkArgument(!isLoaded(resourceLocation), "Duplicate module: %s", resourceLocation);
		LOADED_MODULES.add(resourceLocation);
		MODULES.put(resourceLocation, new ModuleInfo(resourceLocation, module, context));
	}

	static void handleRegister(RegistryEvent.Register<?> event) {
		MODULES.values().forEach(info -> info.handleRegister(event));
		ModLoadingContext.get().setActiveContainer(null, null);
	}

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
		if (!DatagenModLoader.isRunningDataGen()) {
			MODULES.clear();
			MODULES = Collections.EMPTY_MAP;
		}
	}

	public static void fire(Consumer<ModuleInfo> consumer) {
		MODULES.values().forEach(consumer);
	}

}
