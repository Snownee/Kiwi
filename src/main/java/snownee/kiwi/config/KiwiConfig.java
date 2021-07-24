package snownee.kiwi.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.minecraftforge.fml.config.ModConfig;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface KiwiConfig {

	/**
     * File name of this config. modid-type.toml by default
     */
	String value() default "";

	ModConfig.Type type() default ModConfig.Type.COMMON;

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface Path {
		String value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface Comment {
		String[] value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface Translation {
		String value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface Range {
		double min() default Double.NaN;

		double max() default Double.NaN;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface LevelRestart {
	}
}
