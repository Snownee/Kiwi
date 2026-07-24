package snownee.kiwi;

import java.util.List;
import java.util.Map;

import snownee.kiwi.config.ConfigUI;
import snownee.kiwi.config.KiwiConfig;

@KiwiConfig
public final class KiwiCommonConfig {

	public static Map<String, Object> vars = Map.of("Author", "Snownee");

	public static boolean getBooleanVar(String key) {
		return vars.get(key) instanceof Boolean && (Boolean) vars.get(key);
	}

	@KiwiConfig.Path("eval.printExpression")
	public static boolean evalPrintExpression = false;

	@KiwiConfig.Path("kSwitch.creativeOnly")
	public static boolean kSwitchCreativeOnly;

	@ConfigUI.Typed(String.class)
	@KiwiConfig.Path("customization.disableStonecuttingNamespaces")
	public static List<String> disableStonecuttingNamespaces = List.of();
	@KiwiConfig.Path("customization.kSwitchAutoStonecuttingRecipes")
	public static boolean kSwitchAutoStonecuttingRecipes = true;
}
