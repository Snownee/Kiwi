package snownee.kiwi;

import snownee.kiwi.config.ConfigUI;
import snownee.kiwi.config.KiwiConfig;
import snownee.kiwi.config.KiwiConfig.ConfigType;

@KiwiConfig(type = ConfigType.CLIENT)
public final class KiwiClientConfig {

	@ConfigUI.Hide
	public static String contributorCosmetic = "";

	public static boolean globalTooltip;

	@KiwiConfig.GameRestart
	public static boolean noMicrosoftTelemetry = true;

	@KiwiConfig.Path("debug.tagsTooltip")
	public static boolean tagsTooltip = true;

	@KiwiConfig.Path("debug.NBTTooltip")
	public static boolean nbtTooltip = true;

	@ConfigUI.Hide
	@KiwiConfig.Path("debug.debugTooltipMsg")
	public static boolean debugTooltipMsg = true;

	@KiwiConfig.Path("suppressExperimentalSettingsWarning")
	public static boolean suppressExperimentalWarning;

}
