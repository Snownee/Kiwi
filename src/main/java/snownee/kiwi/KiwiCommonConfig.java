package snownee.kiwi;

import java.util.Map;

import snownee.kiwi.config.KiwiConfig;

@KiwiConfig
public final class KiwiCommonConfig {

	public static Map<String, Object> vars = Map.of("Author", "Snownee");

	public static boolean getBooleanVar(String key) {
		return vars.get(key) instanceof Boolean && (Boolean) vars.get(key);
	}

	@KiwiConfig.Path("kSwitch.creativeOnly")
	public static boolean kSwitchCreativeOnly = true;
}
