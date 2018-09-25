package snownee.kiwi;

import net.minecraftforge.common.config.Config;

@Config(modid = Kiwi.MODID, name = Kiwi.MODID, category = "")
@Config.LangKey("kiwi.config")
public class KiwiConfig
{
    private KiwiConfig()
    {
        // No-op, no instance for you
    }

    @Config.Comment("General settings of Kiwi.")
    @Config.LangKey("kiwi.config.general")
    @Config.Name("General")
    public static final General GENERAL = new General();

    public static final class General
    {
        General()
        {
            // No-op. Package-level access.
        }

        @Config.Comment("A list of preferred Mod IDs that results of Cuisine processes should stem from")
        @Config.LangKey("kiwi.config.general.oredict_preference")
        @Config.Name("OreDict Preference")
        public String[] orePreference = new String[] { "cuisine", "minecraft" };
    }
}
