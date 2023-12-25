package snownee.kiwi.test;

import snownee.kiwi.config.KiwiConfig;
import snownee.kiwi.config.KiwiConfig.Range;

public class MyConfig {

	@KiwiConfig.Path("debugMode.enabled")
	public static boolean debug = true;

	@Range(min = 0, max = 1)
	public static float treasureChance = 0.5f;

	@KiwiConfig.Listen("debugMode.enabled")
	public static void onToggleDebugMode(String path) {
		System.out.println("Debug Mode: " + debug);
	}

}
