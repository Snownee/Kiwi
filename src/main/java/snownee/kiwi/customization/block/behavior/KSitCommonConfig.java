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
	@KiwiConfig.Range(min = 0.1, max = 1)
	public static float sitActionReachDistanceRatio = 0.75f;
	public static boolean requireEmptyHand = false;
	public static boolean allowClickBlockBottomToSit = false;
}