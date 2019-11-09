package snownee.kiwi.util;

import java.text.DecimalFormat;
import java.text.MessageFormat;

import javax.annotation.Nullable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class Util {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###,###");
    public static final MessageFormat MESSAGE_FORMAT = new MessageFormat("{0,number,#.#}");

    public static String color(int color) {
        return String.format("\u00A7x%06x", color & 0x00FFFFFF);
    }

    public static String formatComma(long number) {
        return DECIMAL_FORMAT.format(number);
    }

    public static String formatCompact(long number) {
        int unit = 1000;
        if (number < unit) {
            return Long.toString(number);
        }
        int exp = (int) (Math.log(number) / Math.log(unit));
        if (exp - 1 >= 0 && exp - 1 < 6) {
            char pre = "kMGTPE".charAt(exp - 1);
            return MESSAGE_FORMAT.format(new Double[] { number / Math.pow(unit, exp) }) + pre;
        }
        return Long.toString(number);
    }

    public static String trimRL(ResourceLocation rl) {
        return rl.getNamespace().equals("minecraft") ? rl.getPath() : rl.toString();
    }

    public static String trimRL(String rl) {
        if (rl.startsWith("minecraft:")) {
            return rl.substring("minecraft:".length());
        } else {
            return rl;
        }
    }

    @Nullable
    public static ResourceLocation RL(@Nullable String string) {
        try {
            return ResourceLocation.tryCreate(string);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @since 2.4.2
     */
    @Nullable
    public static ResourceLocation RL(@Nullable String string, String defaultNamespace) {
        if (!string.contains(":")) {
            string = defaultNamespace + ":" + string;
        }
        return RL(string);
    }

    public static String getTextureItem(ItemStack stack, String mark) {
        NBTHelper data = NBTHelper.of(stack);
        ResourceLocation rl = RL(data.getString("BlockEntityTag.Items." + mark));
        if (rl != null) {
            Item item = ForgeRegistries.ITEMS.getValue(rl);
            if (item != null) {
                return item.getTranslationKey();
            }
        }
        return "";
    }
}
