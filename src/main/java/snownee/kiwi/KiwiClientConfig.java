package snownee.kiwi;

import snownee.kiwi.config.ConfigUI;
import snownee.kiwi.config.KiwiConfig;
import snownee.kiwi.config.KiwiConfig.Comment;
import snownee.kiwi.config.KiwiConfig.ConfigType;
import snownee.kiwi.config.KiwiConfig.Path;
import snownee.kiwi.config.KiwiConfig.Range;

@KiwiConfig(type = ConfigType.CLIENT)
public final class KiwiClientConfig {

	@ConfigUI.Hide
	public static String contributorCosmetic = "";

	@Comment("Show customized tooltips from any item. Mainly for pack devs")
	public static boolean globalTooltip = false;

	@Comment("Max line width shown in description of tooltips")
	@Range(min = 50)
	public static int tooltipWrapWidth = 200;

	@Comment("Show item tags in advanced tooltips")
	@Path("debug.tagsTooltip")
	public static boolean tagsTooltip = true;

	@Comment("Show item nbt in advanced tooltips while holding shift")
	@Path("debug.NBTTooltip")
	public static boolean nbtTooltip = true;

	@Comment("Allowed values: vanilla, kiwi")
	@Path("debug.NBTTooltipFormatter")
	public static String debugTooltipNBTFormatter = "vanilla";

}
