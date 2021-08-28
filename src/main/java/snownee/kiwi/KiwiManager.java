package snownee.kiwi;

import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.CrashReportCallables;
import net.minecraftforge.fml.ModLoadingContext;

public final class KiwiManager {
	public static final Map<ResourceLocation, ModuleInfo> MODULES = Maps.newLinkedHashMap();

	static {
		CrashReportCallables.registerCrashCallable("Kiwi Modules", () -> ("\n" + MODULES.keySet().stream().map(ResourceLocation::toString).sorted(StringUtils::compare).collect(Collectors.joining("\n\t\t", "\t\t", ""))));
	}

	private KiwiManager() {
	}

	public static void addInstance(ResourceLocation resourceLocation, AbstractModule module, ModContext context) {
		if (MODULES.containsKey(resourceLocation)) {
			Kiwi.logger.error(Kiwi.MARKER, "Found a duplicate module name {}, skipping.", resourceLocation);
		} else {
			MODULES.put(resourceLocation, new ModuleInfo(resourceLocation, module, context));
		}
	}

	static void handleRegister(RegistryEvent.Register<?> event) {
		MODULES.values().forEach(info -> info.handleRegister(event));
		ModLoadingContext.get().setActiveContainer(null);
	}

}
