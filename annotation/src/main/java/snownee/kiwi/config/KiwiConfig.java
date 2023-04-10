package snownee.kiwi.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Locale;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface KiwiConfig {

	/**
	 * File name of this config. modid-type.toml by default
	 */
	String value() default "";

	ConfigType type() default ConfigType.COMMON;

	enum ConfigType {
		COMMON, CLIENT, SERVER;

		public String extension() {
			return name().toLowerCase(Locale.ENGLISH);
		}
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

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface GameRestart {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.FIELD, ElementType.TYPE})
	@interface Path {
		/**
		 * The path of the value in the configuration. Each key is separated by a dot.
		 * <p>
		 * Use {@link AdvancedPath} if you have a key that contains dots.
		 *
		 * @return the path in the config
		 */
		String value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.FIELD, ElementType.TYPE})
	@interface AdvancedPath {
		/**
		 * The path of the value in the configuration. Each key is given by an element of the array.
		 *
		 * @return the path in the config
		 */
		String[] value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@Repeatable(Listens.class)
	@interface Listen {
		String value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@interface Listens {
		Listen[] value();
	}
}
