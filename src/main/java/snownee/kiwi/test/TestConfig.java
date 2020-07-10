package snownee.kiwi.test;

import java.util.Arrays;
import java.util.List;

import com.electronwill.nightconfig.core.conversion.Path;

import net.minecraftforge.fml.config.ModConfig;
import snownee.kiwi.config.KiwiConfig;
import snownee.kiwi.config.KiwiConfig.Comment;
import snownee.kiwi.config.KiwiConfig.Range;
import snownee.kiwi.config.KiwiConfig.WorldRestart;

@KiwiConfig("test")
public class TestConfig {

    public static int intValue = 5;

    @Range(min = 0, max = 114514)
    public static long longValue = 6;

    @Range(min = 0, max = 10.5)
    public static double doubleValue = 7.5;

    @Path("Malay.P")
    @Comment("\\ MalayP /")
    public static String strValue = "MalayP";

    public static boolean booleanValue = true;

    public static List<String> listValue = Arrays.asList("test");

    @WorldRestart
    public static ModConfig.Type enumValue = ModConfig.Type.COMMON;

}
