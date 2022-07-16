package snownee.kiwi;

import snownee.kiwi.config.ConfigUI;
import snownee.kiwi.config.KiwiConfig;
import snownee.kiwi.config.KiwiConfig.Comment;
import snownee.kiwi.config.KiwiConfig.ConfigType;
import snownee.kiwi.config.KiwiConfig.Path;

@KiwiConfig(type = ConfigType.CLIENT)
public final class KiwiClientConfig {

	@ConfigUI.Hide
	public static String contributorCosmetic = "";

	@Comment("Show customized tooltips from any item.\nMainly for pack devs")
	public static boolean globalTooltip = false;

	public static boolean noMicrosoftTelemetry = true;

	@Comment("Show item tags in advanced tooltips")
	@Path("debug.tagsTooltip")
	public static boolean tagsTooltip = true;

	@Comment("Show item nbt in advanced tooltips while holding shift")
	@Path("debug.NBTTooltip")
	public static boolean nbtTooltip = true;

	@ConfigUI.Hide
	@Comment("Show tips about disabling debug tooltips")
	@Path("debug.debugTooltipMsg")
	public static boolean debugTooltipMsg = true;

}
