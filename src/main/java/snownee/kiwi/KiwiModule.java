package snownee.kiwi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface KiwiModule
{
    String modid();

    /**
     * Unique id of module. Same as modid if empty
     */
    String name() default "";

    /**
     * Module will be registered only if dependent mods are loaded.
     * You can use ';' to separate multiple mod ids.
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
    public @interface Optional
    {
        boolean disabledByDefault() default false;
    }

    /**
     * 
     * Item group this module belongs to.
     * You can input vanilla group id, such as 'buildingBlocks', 'misc'
     * If empty, Kiwi will catch the first ItemGroup in this module.
     * 
     * @see KiwiManager#addItemGroup(String, String, net.minecraft.item.ItemGroup)
     * @author Snownee
     *
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Group
    {
        String value() default "";
    }
}
