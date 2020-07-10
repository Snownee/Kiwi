package snownee.kiwi.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.fml.config.ModConfig;
import snownee.kiwi.Kiwi;

public class KiwiConfigManager {

    private static final List<ConfigHandler> allConfigs = Lists.newLinkedList();
    private static final Map<String, ConfigHandler> masterConfigs = Maps.newHashMap();
    public static final Map<ResourceLocation, BooleanValue> modules = Maps.newHashMap();

    public static void register(ConfigHandler configHandler) {
        allConfigs.add(configHandler);
        if (configHandler.isMaster()) {
            masterConfigs.put(configHandler.getModId(), configHandler);
        }
    }

    public static void init() {
        Collections.sort(allConfigs, (a, b) -> a.getFileName().compareTo(b.getFileName()));
        Set<String> settledMods = Sets.newHashSet();
        for (ConfigHandler config : allConfigs) {
            if (config.isMaster()) {
                settledMods.add(config.getModId());
            }
        }
        for (ConfigHandler config : allConfigs) {
            if (!config.isMaster() && config.getType() == ModConfig.Type.COMMON && !settledMods.contains(config.getModId())) {
                config.setMaster(true);
            }
            config.init();
        }
    }

    public static void defineModules(String modId, Builder builder) {
        builder.push("modules");
        for (Entry<ResourceLocation, Boolean> entry : Kiwi.defaultOptions.entrySet()) {
            ResourceLocation rl = entry.getKey();
            if (rl.getNamespace().equals(modId)) {
                modules.put(rl, builder.define(rl.getPath(), !entry.getValue().booleanValue()));
            }
        }
        builder.pop();
    }

}
