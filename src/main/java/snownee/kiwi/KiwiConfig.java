package snownee.kiwi;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.google.common.collect.Maps;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.thread.EffectiveSide;

public class KiwiConfig
{
    static final ForgeConfigSpec spec;
    static UnmodifiableConfig config;

    public static ConfigValue<List<String>> orePreference;
    public static BooleanValue tooltipRequiresShift;
    public static IntValue tooltipWrapWidth;
    public static BooleanValue replaceDefaultFontRenderer;
    public static Map<ResourceLocation, BooleanValue> modules = Maps.newHashMap();

    static
    {
        final Pair<KiwiConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(KiwiConfig::new);
        spec = specPair.getRight();
    }

    private KiwiConfig(ForgeConfigSpec.Builder builder)
    {
        /* off */
        orePreference = builder
                .comment("A list of preferred Mod IDs that results of processes should stem from")
                .translation("kiwi.config.orePreference")
                .define("orePreference", Collections.singletonList("minecraft"), o -> o instanceof String && isValidNamespace((String) o));
        
        builder.push("modules");
        
        for (Entry<ResourceLocation, Boolean> entry : Kiwi.defaultOptions.entrySet())
        {
            ResourceLocation rl = entry.getKey();
            modules.put(rl, builder.define(rl.getNamespace() + "." + rl.getPath(), entry.getValue().booleanValue()));
        }
        
        builder.pop();
        
        if (EffectiveSide.get() == LogicalSide.SERVER) return;
        
        builder.push("client");
        
        tooltipRequiresShift = builder
                .comment("Tooltips require pressing shift to be shown")
                .translation("kiwi.config.tooltipRequiresShift")
                .define("tooltipRequiresShift", false);
        
        tooltipWrapWidth = builder
                .comment("Max line width shown in description of tooltips")
                .translation("kiwi.config.tooltipWrapWidth")
                .defineInRange("tooltipWrapWidth", 100, 50, Integer.MAX_VALUE);
        
        replaceDefaultFontRenderer = builder
                .comment("Use §x (almost) everywhere. Fix MC-109260. Do NOT enable this unless you know what you are doing")
                .translation("kiwi.config.replaceDefaultFontRenderer")
                .define("replaceDefaultFontRenderer", false);
        /* on */
    }

    private static boolean isValidNamespace(String namespace)
    {
        return namespace.chars().allMatch((c) -> {
            return c == 95 || c == 45 || c >= 97 && c <= 122 || c >= 48 && c <= 57 || c == 46;
        });
    }
}
