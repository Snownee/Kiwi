package snownee.kiwi;

import java.util.Locale;

import org.apache.commons.lang3.tuple.Pair;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import snownee.kiwi.util.Util;

@EventBusSubscriber(bus = Bus.MOD)
public final class KiwiModConfig {
    static final ForgeConfigSpec spec;

    public static ResourceLocation contributorEffect = null;
    public static boolean globalTooltip = false;
    public static int tooltipWrapWidth = 500;
    public static boolean debugTooltip = true;
    public static String debugTooltipNBTFormatter = "vanilla";

    private static ConfigValue<String> contributorEffectCfg;
    private static BooleanValue globalTooltipCfg;
    private static IntValue tooltipWrapWidthCfg;
    private static BooleanValue debugTooltipCfg;
    private static ConfigValue<String> debugTooltipNBTFormatterCfg;

    static {
        final Pair<KiwiModConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(KiwiModConfig::new);
        spec = specPair.getRight();
    }

    private KiwiModConfig(ForgeConfigSpec.Builder builder) {
        /* off */

        contributorEffectCfg = builder.define("contributorEffect", "");

        globalTooltipCfg = builder
                .comment("Show customized tooltips from any item. Mainly for pack devs")
                .translation("kiwi.config.globalTooltip")
                .define("globalTooltip", globalTooltip);

        tooltipWrapWidthCfg = builder
                .comment("Max line width shown in description of tooltips")
                .translation("kiwi.config.tooltipWrapWidth")
                .defineInRange("tooltipWrapWidth", tooltipWrapWidth, 50, Integer.MAX_VALUE);

        debugTooltipCfg = builder
                .comment("Show item tags and nbt in advanced tooltips")
                .translation("kiwi.config.debugTooltip")
                .define("debugTooltip", debugTooltip);

        debugTooltipNBTFormatterCfg = builder
                .comment("Allowed values: vanilla, kiwi")
                .translation("kiwi.config.tooltipNBTFormatter")
                .define("debugTooltipNBTFormatter", debugTooltipNBTFormatter);

        /* on */
    }

    public static void refresh() {
        contributorEffect = Util.RL(contributorEffectCfg.get());
        globalTooltip = globalTooltipCfg.get();
        tooltipWrapWidth = tooltipWrapWidthCfg.get();
        debugTooltip = debugTooltipCfg.get();
        debugTooltipNBTFormatter = debugTooltipNBTFormatterCfg.get().toLowerCase(Locale.ENGLISH);
    }

    @SubscribeEvent
    public static void onFileChange(ModConfig.Reloading event) {
        ((CommentedFileConfig) event.getConfig().getConfigData()).load();
        refresh();
    }
}
