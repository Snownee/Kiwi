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

	@KiwiConfig.Path("qol.suppressExperimentalSettingsWarning")
	public static boolean suppressExperimentalWarning;

	@KiwiConfig.Path("qol.hideDataComponentsTooltip")
	public static boolean hideDataComponentsTooltip;

	@KiwiConfig.Path("qol.titleScreenNoFade")
	public static boolean titleScreenNoFade;

	@KiwiConfig.Path("qol.loadingOverlayNoFade")
	public static boolean loadingOverlayNoFade;

	@KiwiConfig.Path("debug.tagsTooltip")
	public static boolean tagsTooltip = true;

	@KiwiConfig.Path("debug.tagsPerPage")
	@KiwiConfig.Range(min = 0)
	public static int tagsTooltipTagsPerPage = 6;

	@KiwiConfig.Path("debug.tagsTooltipAppendKeybindHint")
	public static boolean tagsTooltipAppendKeybindHint = true;

	@ConfigUI.Hide
	@KiwiConfig.Path("debug.debugTooltipMsg")
	public static boolean debugTooltipMsg = true;
}
