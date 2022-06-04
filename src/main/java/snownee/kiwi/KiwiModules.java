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

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.data.loading.DatagenModLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.CrashReportCallables;
import net.minecraftforge.fml.ModLoadingContext;

public final class KiwiModules {
	private static Map<ResourceLocation, ModuleInfo> MODULES = Maps.newLinkedHashMap();
	private static final Set<ResourceLocation> LOADED_MODULES = Sets.newHashSet();

	static {
		CrashReportCallables.registerCrashCallable("Kiwi Modules", () -> ("\n" + LOADED_MODULES.stream().map(ResourceLocation::toString).sorted(StringUtils::compare).collect(Collectors.joining("\n\t\t", "\t\t", ""))));
	}

	private KiwiModules() {
	}

	public static void add(ResourceLocation resourceLocation, AbstractModule module, ModContext context) {
		Preconditions.checkArgument(!isLoaded(resourceLocation), "Duplicate module: %s", resourceLocation);
		LOADED_MODULES.add(resourceLocation);
		MODULES.put(resourceLocation, new ModuleInfo(resourceLocation, module, context));
	}

	@SubscribeEvent
	public static void handleRegister(RegistryEvent.Register<Block> event) {
		MODULES.values().forEach(info -> info.registries.registries.keySet().forEach(info::handleRegister));
		ModLoadingContext.get().setActiveContainer(null);
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
