package snownee.kiwi.customization.block.behavior;

import snownee.kiwi.KiwiModule;
import snownee.kiwi.config.KiwiConfig;

@KiwiModule.Skip
@KiwiConfig("ksit-common")
public class KSitCommonConfig {
	public static boolean sitOnSlab = true;
	public static boolean sitOnStairs = true;
	public static boolean sitOnCarpet = true;
	public static boolean sitOnBed = true;
}
