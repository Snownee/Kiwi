package snownee.kiwi.test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import snownee.kiwi.config.ConfigUI.Typed;
import snownee.kiwi.config.ConfigUI.TextDescription;
import snownee.kiwi.config.KiwiConfig;
import snownee.kiwi.config.KiwiConfig.ConfigType;
import snownee.kiwi.config.KiwiConfig.LevelRestart;
import snownee.kiwi.config.KiwiConfig.Path;
import snownee.kiwi.config.KiwiConfig.Range;

@KiwiConfig(value = "test", type = ConfigType.COMMON)
public class TestConfig {

	public static int intValue = 5;

	@Range(min = 0, max = 114514)
	public static long longValue = 6;

	@Range(min = 0, max = 10.5)
	public static float floatValue = 7.5f;

	@Path("Malay.P")
	public static String strValue = "MalayP";

	public static boolean booleanValue = true;

	@Typed(String.class)
	@TextDescription(value = "1\n2\n3", after = true)
	public static List<String> listValue = Arrays.asList("test");

	@TextDescription("Test2")
	public static String emptyStr;

	public static Map<String, Object> testMap = Map.of("datapack:custom", Map.of("1.2", "2"));

	@LevelRestart
	public static ConfigType enumValue = ConfigType.COMMON;

	@KiwiConfig.Listen("Malay.P")
	public static void onChanged(String path) {
		// do sth
		System.out.println(path);
		System.out.println(strValue);
	}

}
