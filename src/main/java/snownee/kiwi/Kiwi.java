package snownee.kiwi;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.objectweb.asm.Type;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.utils.StringUtils;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.LifecycleEventProvider;
import net.minecraftforge.fml.LifecycleEventProvider.LifecycleEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.ModLoadingStage;
import net.minecraftforge.fml.ModLoadingWarning;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.fml.loading.toposort.TopologicalSort;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.ModFileScanData.AnnotationData;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryManager;
import snownee.kiwi.KiwiModule.Group;
import snownee.kiwi.KiwiModule.Subscriber;
import snownee.kiwi.KiwiModule.Subscriber.Bus;
import snownee.kiwi.crafting.FullBlockIngredient;
import snownee.kiwi.crafting.ModuleLoadedCondition;
import snownee.kiwi.schedule.Scheduler;
import snownee.kiwi.util.Util;

@Mod(Kiwi.MODID)
@EventBusSubscriber
public class Kiwi {
    public static final String MODID = "kiwi";
    public static final String NAME = "Kiwi";

    public static Logger logger = LogManager.getLogger(Kiwi.NAME);
    static final Marker MARKER = MarkerManager.getMarker("Init");
    static Field FIELD_EXTENSION;

    private static final class Info {
        final ResourceLocation id;
        final String className;
        final List<ResourceLocation> moduleRules = Lists.newLinkedList();

        public Info(ResourceLocation id, String className) {
            this.id = id;
            this.className = className;
        }
    }

    static {
        try {
            FIELD_EXTENSION = ModContainer.class.getDeclaredField("contextExtension");
            FIELD_EXTENSION.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static Multimap<String, AnnotationData> moduleData = ArrayListMultimap.create();
    public static Map<ResourceLocation, Boolean> defaultOptions = Maps.newHashMap();

    public Kiwi() {
        final Type KIWI_MODULE = Type.getType(KiwiModule.class);
        final Type OPTIONAL_MODULE = Type.getType(KiwiModule.Optional.class);

        Map<Type, AnnotationData> moduleToOptional = Maps.newHashMap();
        for (ModInfo info : ModList.get().getMods()) {
            ModFileInfo modFileInfo = info.getOwningFile();
            if (modFileInfo != null) {
                for (AnnotationData annotationData : modFileInfo.getFile().getScanResult().getAnnotations()) {
                    if (KIWI_MODULE.equals(annotationData.getAnnotationType())) {
                        String modid = (String) annotationData.getAnnotationData().get("modid");
                        moduleData.put(Strings.isNullOrEmpty(modid) ? info.getModId() : modid, annotationData);
                    } else if (OPTIONAL_MODULE.equals(annotationData.getAnnotationType())) {
                        moduleToOptional.put(annotationData.getClassType(), annotationData);
                    }
                }
            }
        }

        logger.info(MARKER, "Processing " + moduleData.size() + " KiwiModule annotations");

        for (Entry<String, AnnotationData> entry : moduleData.entries()) {
            AnnotationData optional = moduleToOptional.get(entry.getValue().getClassType());
            if (optional != null) {
                String modid = entry.getKey();
                if (!ModList.get().isLoaded(modid)) {
                    continue;
                }

                String name = (String) entry.getValue().getAnnotationData().get("name");
                if (Strings.isNullOrEmpty(name)) {
                    name = modid;
                }

                Boolean disabledByDefault = (Boolean) optional.getAnnotationData().get("disabledByDefault");
                if (disabledByDefault == null) {
                    disabledByDefault = Boolean.FALSE;
                }
                defaultOptions.put(new ResourceLocation(modid, name), disabledByDefault);
            }
        }

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, KiwiConfig.spec, MODID + ".toml");
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::preInit);
        modEventBus.addListener(this::init);
        modEventBus.addListener(this::clientInit);
        MinecraftForge.EVENT_BUS.addListener(this::serverInit);
        modEventBus.addListener(this::postInit);
        modEventBus.addListener(this::loadComplete);
    }

    public void preInit(RegistryEvent.NewRegistry event) {
        try {
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

            Field fTriggers = ModContainer.class.getDeclaredField("triggerMap");
            fTriggers.setAccessible(true);
            Map<ModLoadingStage, Consumer<LifecycleEventProvider.LifecycleEvent>> triggerMap = (Map<ModLoadingStage, Consumer<LifecycleEvent>>) fTriggers.get(myContainer);
            Consumer<LifecycleEvent> consumer = triggerMap.get(ModLoadingStage.LOAD_REGISTRIES).andThen(e -> {
                KiwiManager.handleRegister((RegistryEvent.Register<?>) e.getOrBuildEvent(myContainer));
            });
            triggerMap.put(ModLoadingStage.LOAD_REGISTRIES, consumer);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            logger.error(MARKER, "Kiwi failed to load infrastructures. Please report to developer!");
            logger.catching(e);
            return;
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

        final Map<ResourceLocation, Info> infos = Maps.newHashMap();
        boolean checkDep = false;

        load:
        for (Entry<String, AnnotationData> entry : moduleData.entries()) {
            AnnotationData module = entry.getValue();
            String modid = entry.getKey();
            if (!ModList.get().isLoaded(modid)) {
                continue;
            }

            String name = (String) module.getAnnotationData().get("name");
            if (Strings.isNullOrEmpty(name)) {
                name = modid;
            }

            ResourceLocation rl = new ResourceLocation(modid, name);
            if (KiwiConfig.modules.containsKey(rl) && !KiwiConfig.modules.get(rl).get()) {
                continue;
            }

            Info info = new Info(rl, module.getClassType().getClassName());

            String dependencies = (String) module.getAnnotationData().get("dependencies");
            /* off */
            List<String> rules = StringUtils.split(Strings.nullToEmpty(dependencies), ';').stream()
                    .filter(s -> !Strings.isNullOrEmpty(s))
                    .collect(Collectors.toList());
            /* on */

            for (String rule : rules) {
                if (rule.startsWith("@")) {
                    info.moduleRules.add(Util.RL(rule.substring(1), modid));
                    checkDep = true;
                } else if (!ModList.get().isLoaded(rule)) {
                    continue load;
                }
            }
            infos.put(rl, info);
        }

        List<ResourceLocation> list = null;
        if (checkDep) {
            List<Info> errorList = Lists.newLinkedList();
            for (Info i : infos.values()) {
                for (ResourceLocation id : i.moduleRules) {
                    if (!infos.containsKey(id)) {
                        errorList.add(i);
                        break;
                    }
                }
            }
            for (Info i : errorList) {
                IModInfo modInfo = ModList.get().getModContainerById(i.id.getNamespace()).get().getModInfo();
                String dependencies = org.apache.commons.lang3.StringUtils.join(i.moduleRules, ", ");
                ModLoader.get().addWarning(new ModLoadingWarning(modInfo, ModLoadingStage.ERROR, "msg.kiwi.no_dependencies", i.id, dependencies));
            }
            if (!errorList.isEmpty()) {
                return;
            }
            MutableGraph<ResourceLocation> graph = GraphBuilder.directed().allowsSelfLoops(false).expectedNodeCount(infos.size()).build();
            infos.keySet().forEach(graph::addNode);
            infos.values().forEach($ -> {
                $.moduleRules.forEach(r -> graph.putEdge(r, $.id));
            });
            list = TopologicalSort.topologicalSort(graph, null);
        } else {
            list = ImmutableList.copyOf(infos.keySet());
        }

        for (ResourceLocation id : list) {
            Info info = infos.get(id);
            ModContext context = new ModContext(id.getNamespace());
            context.setActiveContainer();

            try {
                Class<?> clazz = Class.forName(info.className);
                AbstractModule instance = (AbstractModule) clazz.newInstance();
                KiwiManager.addInstance(id, instance, context);
            } catch (InstantiationException | IllegalAccessException | ClassCastException | ClassNotFoundException e) {
                logger.error(MARKER, "Kiwi failed to initialize module class: {}", info.className);
                logger.catching(e);
                continue;
            }

            ModLoadingContext.get().setActiveContainer(null, null);
        }

        moduleData.clear();
        moduleData = null;
        defaultOptions.clear();
        defaultOptions = null;

        Util.class.hashCode();
        Object2IntMap<Class<?>> counter = new Object2IntArrayMap<>();
        for (ModuleInfo info : KiwiManager.MODULES.values()) {
            counter.clear();
            info.context.setActiveContainer();
            Subscriber subscriber = info.module.getClass().getAnnotation(Subscriber.class);
            if (subscriber != null && ArrayUtils.contains(subscriber.side(), FMLEnvironment.dist)) {
                for (Bus bus : subscriber.value()) {
                    bus.bus().get().register(info.module);
                }
            }

            boolean useOwnGroup = info.group == null;
            if (useOwnGroup) {
                Group group = info.module.getClass().getAnnotation(Group.class);
                if (group != null) {
                    String val = group.value();
                    if (!val.isEmpty()) {
                        useOwnGroup = false;
                        if (!org.apache.commons.lang3.StringUtils.contains(val, ':') && !KiwiManager.GROUPS.containsKey(val)) {
                            val = info.module.uid.getNamespace() + ":" + val;
                        }
                        ItemGroup itemGroup = KiwiManager.GROUPS.get(val);
                        if (itemGroup != null) {
                            info.group = itemGroup;
                        }
                    }
                }
            }

            String modid = info.module.uid.getNamespace();
            String name = info.module.uid.getPath();

            Item.Properties tmpBuilder = null;
            Field tmpBuilderField = null;
            for (Field field : info.module.getClass().getFields()) {
                if (field.getAnnotation(Skip.class) != null) {
                    continue;
                }

                int mods = field.getModifiers();
                if (!Modifier.isPublic(mods) || !Modifier.isStatic(mods)) {
                    continue;
                }

                String regName;
                Name nameAnnotation = field.getAnnotation(Name.class);
                if (nameAnnotation != null) {
                    regName = nameAnnotation.value();
                } else {
                    regName = field.getName().toLowerCase(Locale.US);
                }

                if (!Modifier.isFinal(mods)) {
                    if (field.getType() == info.module.getClass() && regName.equals("instance")) {
                        try {
                            field.set(null, info.module);
                        } catch (IllegalArgumentException | IllegalAccessException e) {
                            logger.error(MARKER, "Kiwi failed to inject module instance to module class: {}", info.module.uid);
                            logger.catching(e);
                        }
                    }
                    continue;
                }

                Object o = null;
                try {
                    o = field.get(null);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    logger.error(MARKER, "Kiwi failed to catch game object: {}", field);
                    logger.catching(e);
                }
                if (o == null) {
                    continue;
                }
                if (useOwnGroup && info.group == null && o instanceof ItemGroup) {
                    info.group = (ItemGroup) o;
                } else if (o instanceof Item.Properties) {
                    tmpBuilder = (Item.Properties) o;
                    tmpBuilderField = field;
                    continue;
                } else if (o instanceof Block) {
                    if (field.getAnnotation(NoItem.class) != null) {
                        info.noItems.add((Block) o);
                    }
                    checkNoGroup(info, field, o);
                    if (tmpBuilder != null) {
                        info.blockItemBuilders.put((Block) o, tmpBuilder);
                        try {
                            tmpBuilderField.setAccessible(true);
                            Field modifiers = tmpBuilderField.getClass().getDeclaredField("modifiers");
                            modifiers.setAccessible(true);
                            modifiers.setInt(tmpBuilderField, tmpBuilderField.getModifiers() & ~Modifier.FINAL);
                            tmpBuilderField.set(info.module, null);
                            modifiers.setInt(tmpBuilderField, tmpBuilderField.getModifiers() & ~Modifier.FINAL);
                        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                            logger.error(MARKER, "Kiwi failed to clean used item builder: {}", tmpBuilderField);
                            logger.catching(e);
                        }
                    }
                } else if (o instanceof Item) {
                    checkNoGroup(info, field, o);
                }
                if (o instanceof IForgeRegistryEntry<?>) {
                    IForgeRegistryEntry<?> entry = (IForgeRegistryEntry<?>) o;
                    int i = counter.getOrDefault(entry.getRegistryType(), 0);
                    counter.put(entry.getRegistryType(), i + 1);
                    info.register(entry, regName, field);
                }

                tmpBuilder = null;
                tmpBuilderField = null;
            }

            logger.info(MARKER, "Module [{}:{}] initialized", modid, name);
            for (Class<?> clazz : counter.keySet()) {
                IForgeRegistry<?> registry = RegistryManager.ACTIVE.getRegistry((Class<IForgeRegistryEntry<?>>) clazz);
                String k;
                if (registry != null) {
                    k = Util.trimRL(registry.getRegistryName());
                } else {
                    k = "unknown";
                }
                logger.info(MARKER, "    {}: {}", k, counter.getInt(clazz));
            }
        }

        KiwiManager.MODULES.values().forEach(ModuleInfo::preInit);
        ModLoadingContext.get().setActiveContainer(null, null);
    }

    private static void checkNoGroup(ModuleInfo info, Field field, Object o) {
        if (field.getAnnotation(NoGroup.class) != null) {
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

    public void init(FMLCommonSetupEvent event) {
        KiwiConfig.refresh();
        CraftingHelper.register(new ModuleLoadedCondition.Serializer());
        CraftingHelper.register(new ResourceLocation(MODID, "full_block"), FullBlockIngredient.SERIALIZER);

        KiwiManager.MODULES.values().forEach(m -> m.init(event));
        ModLoadingContext.get().setActiveContainer(null, null);
    }

    public void clientInit(FMLClientSetupEvent event) {
        KiwiManager.MODULES.values().forEach(m -> m.clientInit(event));
        ModLoadingContext.get().setActiveContainer(null, null);
    }

    public void serverInit(FMLServerStartingEvent event) {
        KiwiCommand.register(event.getCommandDispatcher(), !event.getServer().isDedicatedServer());

        KiwiManager.MODULES.values().forEach(m -> m.serverInit(event));
        event.getServer().getWorld(DimensionType.OVERWORLD).getSavedData().getOrCreate(() -> Scheduler.INSTANCE, Scheduler.ID);
        ModLoadingContext.get().setActiveContainer(null, null);
    }

    public void postInit(InterModProcessEvent event) {
        KiwiManager.MODULES.values().forEach(ModuleInfo::postInit);
        ModLoadingContext.get().setActiveContainer(null, null);
    }

    public void loadComplete(FMLLoadCompleteEvent event) {
        KiwiManager.GROUPS.clear();
    }

    public static boolean isLoaded(ResourceLocation module) {
        return KiwiManager.MODULES.containsKey(module);
    }

    public static boolean isLoaded(AbstractModule module) {
        return KiwiManager.MODULES.values().stream().map($ -> $.module).anyMatch(module::equals);
    }

    //    @SideOnly(Side.CLIENT)
    //    private static void replaceFontRenderer()
    //    {
    //        Minecraft.getMinecraft().fontRenderer = AdvancedFontRenderer.INSTANCE;
    //    }
}
