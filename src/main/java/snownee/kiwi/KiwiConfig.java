package snownee.kiwi;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.collect.Maps;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.config.ModConfig;

@EventBusSubscriber(bus = Bus.MOD)
public final class KiwiConfig {
    static final ForgeConfigSpec spec;

    public static boolean tooltipRequiresShift = false;
    public static int tooltipWrapWidth = 100;
    public static boolean debugTooltip = true;

    private static BooleanValue tooltipRequiresShiftCfg;
    private static IntValue tooltipWrapWidthCfg;
    private static BooleanValue debugTooltipCfg;
    //public static BooleanValue replaceDefaultFontRendererCfg;
    public static Map<ResourceLocation, BooleanValue> modules = Maps.newHashMap();

    static {
        final Pair<KiwiConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(KiwiConfig::new);
        spec = specPair.getRight();
    }

    private KiwiConfig(ForgeConfigSpec.Builder builder) {
        /* off */
        builder.push("modules");

        for (Entry<ResourceLocation, Boolean> entry : Kiwi.defaultOptions.entrySet())
        {
            ResourceLocation rl = entry.getKey();
            modules.put(rl, builder.define(rl.getNamespace() + "." + rl.getPath(), !entry.getValue().booleanValue()));
        }

        builder.pop();

        if (EffectiveSide.get() == LogicalSide.SERVER) return;

        builder.push("client");

        tooltipRequiresShiftCfg = builder
                .comment("Tooltips require pressing shift to be shown")
                .translation("kiwi.config.tooltipRequiresShift")
                .define("tooltipRequiresShift", tooltipRequiresShift);

        tooltipWrapWidthCfg = builder
                .comment("Max line width shown in description of tooltips")
                .translation("kiwi.config.tooltipWrapWidth")
                .defineInRange("tooltipWrapWidth", tooltipWrapWidth, 50, Integer.MAX_VALUE);

        debugTooltipCfg = builder
                .comment("Show item tags and nbt in advanced tooltips")
                .translation("kiwi.config.debugTooltip")
                .define("debugTooltip", debugTooltip);

        /*
        replaceDefaultFontRendererCfg = builder
                .comment("Use §x (almost) everywhere. Fix MC-109260. Do NOT enable this unless you know what you are doing")
                .translation("kiwi.config.replaceDefaultFontRenderer")
                .define("replaceDefaultFontRenderer", false);
         */
        /* on */
    }

    public static void refresh() {
        tooltipRequiresShift = tooltipRequiresShiftCfg.get();
        tooltipWrapWidth = tooltipWrapWidthCfg.get();
        debugTooltip = debugTooltipCfg.get();
    }

    @SubscribeEvent
    public static void onFileChange(ModConfig.ConfigReloading event) {
        ((CommentedFileConfig) event.getConfig().getConfigData()).load();
        refresh();
    }
}
