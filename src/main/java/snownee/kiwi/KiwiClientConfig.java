package snownee.kiwi;

import net.minecraftforge.fml.config.ModConfig.Type;
import snownee.kiwi.config.KiwiConfig;
import snownee.kiwi.config.KiwiConfig.Comment;
import snownee.kiwi.config.KiwiConfig.Path;
import snownee.kiwi.config.KiwiConfig.Range;
import snownee.kiwi.config.KiwiConfig.Translation;

@KiwiConfig(type = Type.CLIENT)
public final class KiwiClientConfig {

	public static String contributorEffect = "";

	@Comment("Show customized tooltips from any item. Mainly for pack devs")
	@Translation("globalTooltip")
	public static boolean globalTooltip = false;

	@Comment("Max line width shown in description of tooltips")
	@Translation("tooltipWrapWidth")
	@Range(min = 50)
	public static int tooltipWrapWidth1 = 200;

	@Comment("Show item tags in advanced tooltips")
	@Translation("tagsTooltip")
	@Path("debug.tagsTooltip")
	public static boolean tagsTooltip = true;

	@Comment("Show item nbt in advanced tooltips while holding shift")
	@Translation("nbtTooltip")
	@Path("debug.nbtTooltip")
	public static boolean nbtTooltip = true;

	@Comment("Allowed values: vanilla, kiwi")
	@Translation("tooltipNBTFormatter")
	@Path("debug.tooltipNBTFormatter")
	public static String debugTooltipNBTFormatter = "vanilla";

}
