package snownee.kiwi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Supplier;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface KiwiModule {

	String modid() default "";

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
     *
     * Optional module can be disabled in Kiwi's configuration
     * @author Snownee
     *
     */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@interface Optional {
		boolean disabledByDefault() default false;
	}

	/**
     *
     * Item group this module belongs to.
     * You can input vanilla group id, such as 'buildingBlocks', 'misc'
     * If empty, Kiwi will catch the first CreativeModeTab in this module.
     *
     * @author Snownee
     *
     */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.FIELD })
	@interface Category {
		String value() default "";
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@interface Subscriber {
		/**
         * Specify targets to load this event subscriber on. Can be used to avoid loading Client specific events
         * on a dedicated server, for example.
         *
         * @return an array of Dist to load this event subscriber on
         */
		Dist[] side() default { Dist.CLIENT, Dist.DEDICATED_SERVER };

		/**
		 * Specify an alternative bus to listen to
		 *
		 * @return the bus you wish to listen to
		 */
		Bus value() default Bus.FORGE;

		enum Bus {
			/**
			 * The main Forge Event Bus.
			 *
			 * @see MinecraftForge#EVENT_BUS
			 */
			FORGE(() -> MinecraftForge.EVENT_BUS),
			/**
             * The mod specific Event bus.
             * @see FMLJavaModLoadingContext#getModEventBus()
             */
			MOD(() -> FMLJavaModLoadingContext.get().getModEventBus());

			private final Supplier<IEventBus> busSupplier;

			Bus(Supplier<IEventBus> eventBusSupplier) {
				busSupplier = eventBusSupplier;
			}

			public Supplier<IEventBus> bus() {
				return busSupplier;
			}
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@interface LoadingCondition {
		String[] value() default "";
	}
}
