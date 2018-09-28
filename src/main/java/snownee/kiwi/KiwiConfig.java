package snownee.kiwi;

import java.util.HashMap;
import java.util.Map;

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

    @Config.Comment("Toggle optional modules of Kiwi.")
    @Config.LangKey("kiwi.config.modules")
    @Config.Name("Modules")
    public static final Modules MODULES = new Modules();

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

    public static final class Modules
    {
        Modules()
        {
            // No-op. Package-level access.
        }

        @Config.Comment("TODO") // TODO
        @Config.LangKey("kiwi.config.modules.optional_modules")
        @Config.Name("Optional Modules")
        @Config.RequiresMcRestart
        public Map<String, Boolean> modules = new HashMap<>();
    }
}
