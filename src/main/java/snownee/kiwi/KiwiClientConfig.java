package snownee.kiwi;

import snownee.kiwi.config.ConfigUI;
import snownee.kiwi.config.KiwiConfig;
import snownee.kiwi.config.KiwiConfig.ConfigType;
import snownee.kiwi.loader.Platform;

@KiwiConfig(type = ConfigType.CLIENT)
public final class KiwiClientConfig {

	@ConfigUI.Hide
	public static String contributorCosmetic = "";

	public static boolean cosmeticScreenKeybind = true;

	public static boolean globalTooltip;

	@KiwiConfig.GameRestart
	public static boolean noMicrosoftTelemetry = true;

	@KiwiConfig.Path("qol.suppressExperimentalSettingsWarning")
	public static boolean suppressExperimentalWarning;

	@KiwiConfig.Path("qol.hideDataComponentsTooltip")
	public static boolean hideDataComponentsTooltip;

	@KiwiConfig.Path("qol.titleScreenNoFade")
	public static boolean titleScreenNoFade = !Platform.isProduction();

	@KiwiConfig.Path("qol.loadingOverlayNoFade")
	public static boolean loadingOverlayNoFade = !Platform.isProduction();

	@KiwiConfig.Path("qol.superClearChat")
	public static boolean superClearChat;

	@KiwiConfig.Path("qol.noForceBackup")
	public static boolean noForceBackup = !Platform.isProduction();

	@KiwiConfig.Path("debug.tagsTooltip")
	public static boolean tagsTooltip = true;

	@KiwiConfig.Path("debug.F3CopyInInventory")
	public static boolean f3CopyInInventory = true;

	@KiwiConfig.Path("debug.printDataComponentsWhenCopy")
	public static boolean printDataComponentsWhenCopy = true;

	@KiwiConfig.Path("debug.showTranslatedTagsByDefault")
	public static boolean showTranslatedTagsByDefault;

	@KiwiConfig.Path("debug.tagsPerPage")
	@KiwiConfig.Range(min = 0)
	public static int tagsTooltipTagsPerPage = 6;

	@KiwiConfig.Path("debug.tagsTooltipAppendKeybindHint")
	public static boolean tagsTooltipAppendKeybindHint;

	@ConfigUI.Hide
	@KiwiConfig.Path("debug.debugTooltipMsg")
	public static boolean debugTooltipMsg = true;

	@KiwiModule.Skip
	public static boolean exportBlocksMore;
}
