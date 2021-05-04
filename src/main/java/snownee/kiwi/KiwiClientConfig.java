package snownee.kiwi;

import net.minecraftforge.fml.config.ModConfig.Type;
import snownee.kiwi.config.KiwiConfig;
import snownee.kiwi.config.KiwiConfig.Comment;
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

	@Comment("Show item tags and nbt in advanced tooltips")
	@Translation("debugTooltip")
	public static boolean debugTooltip = true;

	@Comment("Allowed values: vanilla, kiwi")
	@Translation("tooltipNBTFormatter")
	public static String debugTooltipNBTFormatter = "vanilla";

}
