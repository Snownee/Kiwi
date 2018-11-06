package snownee.kiwi;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.Logger;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.ASMDataTable.ASMData;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import snownee.kiwi.block.IModBlock;
import snownee.kiwi.item.IModItem;
import snownee.kiwi.potion.PotionMod;

@Mod(modid = Kiwi.MODID, name = Kiwi.NAME, version = "@VERSION_INJECT@", acceptedMinecraftVersions = "[1.12, 1.13)")
public class Kiwi
{
    public static final String MODID = "kiwi";
    public static final String NAME = "Kiwi";

    private static final Kiwi INSTANCE = new Kiwi();

    @Mod.InstanceFactory
    public static Kiwi getInstance()
    {
        return INSTANCE;
    }

    public static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
            throws IllegalArgumentException, IllegalAccessException, InstantiationException, ClassNotFoundException
    {
        logger = event.getModLog();

        ASMDataTable table = event.getAsmData();
        Set<ASMData> allModules = table.getAll(KiwiModule.class.getName());
        logger.info("Processing " + allModules.size() + " KiwiModule annotations");

        for (ASMData data : allModules)
        {
            String modid = (String) data.getAnnotationInfo().get("modid");
            String name = (String) data.getAnnotationInfo().get("name");
            if (name == null)
            {
                name = modid;
            }
            Boolean optional = (Boolean) data.getAnnotationInfo().get("optional");
            if (optional == Boolean.TRUE)
            {
                Boolean enabled = KiwiConfig.MODULES.modules.get(modid + ":" + name);
                if (enabled == null)
                {
                    KiwiConfig.MODULES.modules.put(modid + ":" + name, !Kiwi.MODID.equals(modid));
                    if (Kiwi.MODID.equals(modid))
                    {
                        continue;
                    }
                }
                else if (enabled == Boolean.FALSE)
                {
                    continue;
                }
            }
            String dependency = (String) data.getAnnotationInfo().get("dependency");
            if (dependency != null && !Loader.isModLoaded(dependency))
            {
                continue;
            }
            Class<?> asmClass = Class.forName(data.getClassName());
            IModule instance = asmClass.asSubclass(IModule.class).newInstance();
            KiwiManager.addInstance(new ResourceLocation(modid, name), instance);
        }
        ConfigManager.sync(MODID, Config.Type.INSTANCE);

        for (Entry<ResourceLocation, IModule> entry : KiwiManager.MODULES.entrySet())
        {
            int countBlock = 0;
            int countItem = 0;

            String modid = entry.getKey().getNamespace();
            String name = entry.getKey().getPath();

            for (Field field : entry.getValue().getClass().getFields())
            {
                int mods = field.getModifiers();
                if (!Modifier.isPublic(mods) || !Modifier.isStatic(mods) || !Modifier.isFinal(mods))
                {
                    continue;
                }
                Object o = field.get(null);
                if (o == null)
                {
                    continue;
                }
                if (o instanceof IModBlock)
                {
                    KiwiManager.BLOCKS.put((IModBlock) o, modid);
                    ++countBlock;
                }
                else if (o instanceof IModItem)
                {
                    KiwiManager.ITEMS.put((IModItem) o, modid);
                    ++countItem;
                }
                else if (o instanceof PotionMod)
                {
                    KiwiManager.POTIONS.put((PotionMod) o, modid);
                }
            }

            Kiwi.logger.info("[{}:{}]: Block: {}, Item: {}", modid, name, countBlock, countItem);
            KiwiManager.MODULES.values().forEach(IModule::preInit);
        }

    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        KiwiManager.MODULES.values().forEach(IModule::init);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        KiwiManager.MODULES.values().forEach(IModule::postInit);
        KiwiManager.BLOCKS.clear();
        KiwiManager.BLOCKS = null;
        KiwiManager.ITEMS.clear();
        KiwiManager.ITEMS = null;
        KiwiManager.POTIONS.clear();
        KiwiManager.POTIONS = null;
    }

    public static boolean isOptionalModuleLoaded(String modid, String name)
    {
        return KiwiConfig.MODULES.modules.getOrDefault(modid + ":" + name, false);
    }
}