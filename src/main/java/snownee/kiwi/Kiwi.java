package snownee.kiwi;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.utils.StringUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.forgespi.language.ModFileScanData.AnnotationData;
import snownee.kiwi.KiwiModule.Group;
import snownee.kiwi.crafting.ConditionModuleLoaded;
import snownee.kiwi.util.LootDumper;

@Mod(Kiwi.MODID)
public class Kiwi
{
    public static final String MODID = "kiwi";
    public static final String NAME = "Kiwi";

    public static Logger logger = LogManager.getLogger(Kiwi.NAME);

    public static Field FIELD_EXTENSION;

    static
    {
        try
        {
            FIELD_EXTENSION = ModContainer.class.getDeclaredField("contextExtension");
            FIELD_EXTENSION.setAccessible(true);
        }
        catch (NoSuchFieldException | SecurityException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static List<AnnotationData> moduleData;
    public static Map<ResourceLocation, Boolean> defaultOptions = Maps.newHashMap();

    public Kiwi()
    {
        /* off */
        final Type KIWI_MODULE = Type.getType(KiwiModule.class);
        final Type OPTIONAL_MODULE = Type.getType(KiwiModule.Optional.class);
        
        List<AnnotationData> data = ModList.get().getAllScanData().stream()
                .map(ModFileScanData::getAnnotations)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        
        moduleData = data.stream()
                .filter(a -> KIWI_MODULE.equals(a.getAnnotationType()))
                .collect(Collectors.toList());

        List<Type> moduleTypes = moduleData.stream()
                .map(AnnotationData::getClassType)
                .collect(Collectors.toList());

        Map<Type, AnnotationData> moduleToOptional = Maps.newHashMap();
        
        data.stream()
                .filter(a -> OPTIONAL_MODULE.equals(a.getAnnotationType()))
                .filter(a -> moduleTypes.contains(a.getClassType()))
                .forEach(a -> moduleToOptional.put(a.getClassType(), a));
        /* on */

        logger.info("Processing " + moduleTypes.size() + " KiwiModule annotations");

        for (AnnotationData module : moduleData)
        {
            AnnotationData optional = moduleToOptional.get(module.getClassType());
            if (optional != null)
            {
                String modid = module.getAnnotationData().get("modid").toString();
                if (!ModList.get().isLoaded(modid))
                {
                    continue;
                }

                String name = (String) module.getAnnotationData().get("name");
                if (name == null || name.isEmpty())
                {
                    name = modid;
                }

                Boolean disabledByDefault = (Boolean) optional.getAnnotationData().get("disabledByDefault");
                defaultOptions.put(new ResourceLocation(modid, name), disabledByDefault);
            }
        }

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, KiwiConfig.spec, MODID + ".toml");
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::preInit);
        modEventBus.addListener(this::init);
        modEventBus.addListener(this::clientInit);
        modEventBus.addListener(this::serverInit);
        modEventBus.addListener(this::postInit);
        modEventBus.addListener(this::loadComplete);
    }

    public void preInit(RegistryEvent.NewRegistry event)
    {
        try
        {
            ModContainer myContainer = ModLoadingContext.get().getActiveContainer();
            Field fMap = ModContainer.class.getDeclaredField("configs");
            fMap.setAccessible(true);
            EnumMap<ModConfig.Type, ModConfig> map = (EnumMap<ModConfig.Type, ModConfig>) fMap.get(myContainer);
            ModConfig config = map.get(ModConfig.Type.COMMON);

            CommentedFileConfig configData = config.getHandler().reader(FMLPaths.CONFIGDIR.get()).apply(config);
            Field fCfg = ModConfig.class.getDeclaredField("configData");
            fCfg.setAccessible(true);
            fCfg.set(config, configData);
            config.getSpec().setConfig(configData);
            config.save();
        }
        catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        /* off */
        ImmutableList.of(
                ItemGroup.BUILDING_BLOCKS,
                ItemGroup.DECORATIONS,
                ItemGroup.REDSTONE,
                ItemGroup.TRANSPORTATION,
                ItemGroup.MISC,
                ItemGroup.FOOD,
                ItemGroup.TOOLS,
                ItemGroup.COMBAT,
                ItemGroup.BREWING
                )
        .forEach(g -> KiwiManager.GROUPS.put(g.getPath(), g));
        /* on */

        for (AnnotationData module : moduleData)
        {
            String modid = (String) module.getAnnotationData().get("modid");
            if (!ModList.get().isLoaded(modid))
            {
                continue;
            }

            String name = (String) module.getAnnotationData().get("name");
            if (name == null || name.isEmpty())
            {
                name = modid;
            }

            ResourceLocation rl = new ResourceLocation(modid, name);
            if (KiwiConfig.modules.containsKey(rl) && !KiwiConfig.modules.get(rl).get())
            {
                continue;
            }

            /* off */
            String dependencies = (String) module.getAnnotationData().get("dependencies");
            boolean shouldLoad = dependencies == null || StringUtils.split(dependencies, ';').stream()
                    .filter(s -> !s.isEmpty())
                    .allMatch(s -> ModList.get().isLoaded(s));
            /* on */

            if (!shouldLoad)
            {
                continue;
            }

            ModContext context = new ModContext(modid);
            context.setActiveContainer();

            try
            {
                Class<?> clazz = Class.forName(module.getClassType().getClassName());
                AbstractModule instance = (AbstractModule) clazz.newInstance();
                KiwiManager.addInstance(new ResourceLocation(modid, name), instance, context);
            }
            catch (InstantiationException | IllegalAccessException | ClassCastException | ClassNotFoundException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
                continue;
            }

            ModLoadingContext.get().setActiveContainer(null, null);
        }

        moduleData.clear();
        moduleData = null;
        defaultOptions.clear();
        defaultOptions = null;

        for (ModuleInfo info : KiwiManager.MODULES.values())
        {
            if (info.group != null)
            {
                continue;
            }
            boolean useOwnGroup = false;
            Group group = info.module.getClass().getAnnotation(Group.class);
            if (group != null)
            {
                String val = group.value();
                if (val.isEmpty())
                {
                    useOwnGroup = true;
                }
                else
                {
                    if (!val.matches(":") && !KiwiManager.GROUPS.containsKey(val))
                    {
                        val = info.rl.getNamespace() + ":" + val;
                    }
                    ItemGroup itemGroup = KiwiManager.GROUPS.get(val);
                    if (itemGroup != null)
                    {
                        info.group = itemGroup;
                    }
                }
            }

            int countBlock = 0;
            int countItem = 0;

            String modid = info.rl.getNamespace();
            String name = info.rl.getPath();

            Item.Properties tmpBuilder = null;
            Field tmpBuilderField = null;
            for (Field field : info.module.getClass().getFields())
            {
                int mods = field.getModifiers();
                if (!Modifier.isPublic(mods) || !Modifier.isStatic(mods) || !Modifier.isFinal(mods))
                {
                    continue;
                }

                if (field.getAnnotation(Skip.class) != null)
                {
                    continue;
                }

                String regName;
                Name nameAnnotation = field.getAnnotation(Name.class);
                if (nameAnnotation != null)
                {
                    regName = nameAnnotation.value();
                }
                else
                {
                    regName = field.getName().toLowerCase(Locale.ENGLISH);
                }
                Object o = null;
                try
                {
                    o = field.get(null);
                }
                catch (IllegalArgumentException | IllegalAccessException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (o == null)
                {
                    continue;
                }
                if (useOwnGroup && info.group == null && o instanceof ItemGroup)
                {
                    info.group = (ItemGroup) o;
                }
                else if (o instanceof Item.Properties)
                {
                    tmpBuilder = (Item.Properties) o;
                    tmpBuilderField = field;
                    continue;
                }
                else if (o instanceof Block)
                {
                    if (field.getAnnotation(NoItem.class) != null)
                    {
                        info.noItems.add((Block) o);
                    }
                    checkNoGroup(info, field, o);
                    info.blocks.put((Block) o, regName);
                    if (tmpBuilder != null)
                    {
                        info.blockItemBuilders.put((Block) o, tmpBuilder);
                        try
                        {
                            tmpBuilderField.setAccessible(true);
                            Field modifiers = tmpBuilderField.getClass().getDeclaredField("modifiers");
                            modifiers.setAccessible(true);
                            modifiers.setInt(tmpBuilderField, tmpBuilderField.getModifiers() & ~Modifier.FINAL);
                            tmpBuilderField.set(info.module, null);
                            modifiers.setInt(tmpBuilderField, tmpBuilderField.getModifiers() & ~Modifier.FINAL);
                        }
                        catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e)
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    ++countBlock;
                }
                else if (o instanceof Item)
                {
                    checkNoGroup(info, field, o);
                    info.items.put((Item) o, regName);
                    ++countItem;
                }
                else if (o instanceof Effect)
                {
                    info.effects.put((Effect) o, regName);
                }
                else if (o instanceof Potion)
                {
                    info.potions.put((Potion) o, regName);
                }
                else if (o instanceof IRecipeSerializer<?>)
                {
                    info.recipeTypes.put((IRecipeSerializer<?>) o, regName);
                }
                else if (o instanceof TileEntityType<?>)
                {
                    info.tileTypes.put((TileEntityType<?>) o, regName);
                }
                else if (o instanceof EntityType<?>)
                {
                    info.entityTypes.put((EntityType<?>) o, regName);
                }

                tmpBuilder = null;
                tmpBuilderField = null;
            }

            logger.info("[{}:{}]: Block: {}, Item: {}", modid, name, countBlock, countItem);
        }

        KiwiManager.MODULES.values().forEach(ModuleInfo::preInit);
        ModLoadingContext.get().setActiveContainer(null, null);
    }

    private static void checkNoGroup(ModuleInfo info, Field field, Object o)
    {
        if (field.getAnnotation(NoGroup.class) != null)
        {
            info.noGroups.add(o);
        }
    }
    //    private static String getRegistryName(String name)
    //    {
    //        StringBuilder sb = new StringBuilder();
    //        for (int i = 0; i < name.length(); i++)
    //        {
    //            char c = name.charAt(i);
    //            if (c >= 'A' && c <= 'Z')
    //            {
    //                if (i > 0)
    //                {
    //                    sb.append('_');
    //                }
    //                sb.append(Character.toLowerCase(c));
    //            }
    //            else
    //            {
    //                sb.append(c);
    //            }
    //        }
    //        return sb.toString();
    //    }

    public void init(FMLCommonSetupEvent event)
    {
        CraftingHelper.register(new ResourceLocation(MODID, "is_loaded"), new ConditionModuleLoaded());

        KiwiManager.MODULES.values().forEach(m -> m.init(event));
        ModLoadingContext.get().setActiveContainer(null, null);
    }

    public void clientInit(FMLClientSetupEvent event)
    {
        KiwiManager.MODULES.values().forEach(m -> m.clientInit(event));
        ModLoadingContext.get().setActiveContainer(null, null);
    }

    public void serverInit(FMLDedicatedServerSetupEvent event)
    {
        KiwiManager.MODULES.values().forEach(m -> m.serverInit(event));
        ModLoadingContext.get().setActiveContainer(null, null);
    }

    public void postInit(InterModProcessEvent event)
    {
        KiwiManager.MODULES.values().forEach(ModuleInfo::postInit);
        ModLoadingContext.get().setActiveContainer(null, null);
    }

    public void loadComplete(FMLLoadCompleteEvent event)
    {
        KiwiManager.MODULES.clear();
        KiwiManager.GROUPS.clear();
        if (EffectiveSide.get() == LogicalSide.CLIENT && !KiwiConfig.dumpLootsPattern.get().isEmpty())
        {
            LootDumper.dump(KiwiConfig.dumpLootsPattern.get());
        }
    }

    public static boolean isLoaded(ResourceLocation module)
    {
        return KiwiManager.ENABLED_MODULES.contains(module);
    }

    //    @SideOnly(Side.CLIENT)
    //    private static void replaceFontRenderer()
    //    {
    //        Minecraft.getMinecraft().fontRenderer = AdvancedFontRenderer.INSTANCE;
    //    }
}
