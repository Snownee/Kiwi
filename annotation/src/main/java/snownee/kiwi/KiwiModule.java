package snownee.kiwi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface KiwiModule {

	/**
	 * Unique id of module. "core" by default
	 */
	String value() default "core";

	/**
	 * Module will be registered only if dependent mods or modules are loaded.
	 * You can use ";" to separate multiple mod ids.
	 * You can use "@mod:module" to announce a dependent module
	 */
	String dependencies() default "";

	/**
	 * Optional module can be disabled in Kiwi's configuration
	 *
	 * @author Snownee
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@interface Optional {
		boolean defaultEnabled() default true;
	}

	/**
	 * Item group this module belongs to.
	 * You can input vanilla group id, such as 'buildingBlocks', 'misc'
	 * If empty, Kiwi will catch the first CreativeModeTab in this module.
	 *
	 * @author Snownee
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.TYPE, ElementType.FIELD})
	@interface Category {
		String[] value() default {};

		String[] after() default {};
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@interface LoadingCondition {
		String[] value() default "";
	}

	/**
	 * Rename the entry's registry name. It will be useful if you want to custom
	 * your own BlockItem
	 *
	 * @author Snownee
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface Name {
		String value();
	}

	/**
	 * Set group of this item/block to null
	 *
	 * @author Snownee
	 * @see KiwiModule.Category
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface NoCategory {
	}

	/**
	 * Only used by block. Kiwi will not automatically register its BlockItem
	 *
	 * @author Snownee
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface NoItem {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.TYPE, ElementType.FIELD})
	@interface RenderLayer {
		RenderLayerEnum value();

	}

	/**
	 * Prevent this field being cached by Kiwi
	 *
	 * @author Snownee
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface Skip {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@interface ClientCompanion {
		Class<?> value();
	}

}
