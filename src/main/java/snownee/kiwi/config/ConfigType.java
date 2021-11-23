package snownee.kiwi.config;

import net.minecraftforge.fml.config.ModConfig.Type;

public enum ConfigType {
	COMMON(Type.COMMON), CLIENT(Type.CLIENT), SERVER(Type.SERVER);

	public final Type value;

	ConfigType(Type value) {
		this.value = value;
	}

	public String extension() {
		return value.extension();
	}
}
