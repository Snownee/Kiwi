package snownee.kiwi;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import com.electronwill.nightconfig.core.utils.StringUtils;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.mojang.serialization.Codec;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.StatType;
import net.minecraft.util.valueproviders.FloatProviderType;
import net.minecraft.util.valueproviders.IntProviderType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.decoration.Motive;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSourceType;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSizeType;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosRuleTestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.providers.nbt.LootNbtProviderType;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.score.LootScoreProviderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.ModLoadingStage;
import net.minecraftforge.fml.ModLoadingWarning;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.toposort.TopologicalSort;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import snownee.kiwi.KiwiModule.Category;
import snownee.kiwi.KiwiModule.Name;
import snownee.kiwi.KiwiModule.NoCategory;
import snownee.kiwi.KiwiModule.NoItem;
import snownee.kiwi.KiwiModule.Skip;
import snownee.kiwi.KiwiModule.Subscriber;
import snownee.kiwi.block.def.BlockDefinition;
import snownee.kiwi.block.def.SimpleBlockDefinition;
import snownee.kiwi.client.model.RetextureModel;
import snownee.kiwi.command.KiwiCommand;
import snownee.kiwi.config.ConfigHandler;
import snownee.kiwi.config.KiwiConfig.ConfigType;
import snownee.kiwi.config.KiwiConfigManager;
import snownee.kiwi.loader.AnnotatedTypeLoader;
import snownee.kiwi.loader.KiwiConfiguration;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.loader.event.ClientInitEvent;
import snownee.kiwi.loader.event.InitEvent;
import snownee.kiwi.loader.event.PostInitEvent;
import snownee.kiwi.loader.event.ServerInitEvent;
import snownee.kiwi.network.Networking;
import snownee.kiwi.schedule.Scheduler;
import snownee.kiwi.util.Util;

@Mod(Kiwi.MODID)
public class Kiwi {
	public static final String MODID = "kiwi";
	public static final String NAME = "Kiwi";

	public static Logger logger = LogManager.getLogger(Kiwi.NAME);
	static final Marker MARKER = MarkerManager.getMarker("Init");

	private static final class Info {
		final ResourceLocation id;
		final String className;
		final List<ResourceLocation> moduleRules = Lists.newLinkedList();

		public Info(ResourceLocation id, String className) {
			this.id = id;
			this.className = className;
		}
	}

	private static enum LoadingStage {
		UNINITED, CONSTRUCTED, INITED;
	}

	private static Multimap<String, KiwiAnnotationData> moduleData = ArrayListMultimap.create();
	public static Map<ResourceLocation, Boolean> defaultOptions = Maps.newHashMap();
	private static Map<KiwiAnnotationData, String> conditions = Maps.newHashMap();
	private static RegistryLookup registryLookup = new RegistryLookup();
	private static LoadingStage stage = LoadingStage.UNINITED;

	public Kiwi() throws Exception {
		if (stage != LoadingStage.UNINITED) {
			return;
		}
		stage = LoadingStage.CONSTRUCTED;
		try {
			registerRegistries();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		Map<String, KiwiAnnotationData> classOptionalMap = Maps.newHashMap();
		String dist = Platform.isPhysicalClient() ? "client" : "server";
		List<String> mods = ModList.get().getMods().stream().map(IModInfo::getModId).toList();
		for (String mod : mods) {
			if ("minecraft".equals(mod) || "forge".equals(mod)) {
				continue;
			}
			AnnotatedTypeLoader loader = new AnnotatedTypeLoader(mod);
			KiwiConfiguration configuration = loader.get();
			if (configuration == null) {
				continue;
			}

			for (KiwiAnnotationData module : configuration.modules) {
				if (!checkDist(module, dist))
					continue;
				moduleData.put(mod, module);
			}
			for (KiwiAnnotationData optional : configuration.optionals) {
				if (!checkDist(optional, dist))
					continue;
				classOptionalMap.put(optional.target(), optional);
			}
			for (KiwiAnnotationData condition : configuration.conditions) {
				if (!checkDist(condition, dist))
					continue;
				conditions.put(condition, mod);
			}
			for (KiwiAnnotationData config : configuration.configs) {
				if (!checkDist(config, dist))
					continue;
				ConfigType type = null;
				try {
					type = ConfigType.valueOf((String) config.data().get("type"));
				} catch (Throwable e) {
				}
				type = type == null ? ConfigType.COMMON : type;
				if ((type != ConfigType.CLIENT || Platform.isPhysicalClient())) {
					try {
						Class<?> clazz = Class.forName(config.target());
						String fileName = (String) config.data().get("value");
						boolean master = type == ConfigType.COMMON && Strings.isNullOrEmpty(fileName);
						if (Strings.isNullOrEmpty(fileName)) {
							fileName = String.format("%s-%s", mod, type.extension());
						}
						new ConfigHandler(mod, fileName, type, clazz, master);
					} catch (ClassNotFoundException e) {
						logger.catching(e);
					}
				}
			}
			for (KiwiAnnotationData packet : configuration.packets) {
				if (!checkDist(packet, dist))
					continue;
				Networking.processClass(packet.target(), mod);
			}
		}

		logger.info(MARKER, "Processing " + moduleData.size() + " KiwiModule annotations");

		for (Entry<String, KiwiAnnotationData> entry : moduleData.entries()) {
			KiwiAnnotationData optional = classOptionalMap.get(entry.getValue().target());
			if (optional != null) {
				String modid = entry.getKey();
				if (!Platform.isModLoaded(modid)) {
					continue;
				}

				String name = (String) entry.getValue().data().get("value");
				if (Strings.isNullOrEmpty(name)) {
					name = "core";
				}

				Boolean defaultEnabled = (Boolean) optional.data().get("defaultEnabled");
				if (defaultEnabled == null) {
					defaultEnabled = Boolean.TRUE;
				}
				defaultOptions.put(new ResourceLocation(modid, name), defaultEnabled);
			}
		}

		KiwiConfigManager.init();
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(this::init);
		modEventBus.addListener(this::clientInit);
		MinecraftForge.EVENT_BUS.addListener(this::serverInit);
		modEventBus.addListener(this::postInit);
		modEventBus.addListener(this::loadComplete);
		modEventBus.register(KiwiModules.class);
		if (Platform.isPhysicalClient()) {
			modEventBus.addListener(this::registerModelLoader);
		}
		MinecraftForge.EVENT_BUS.addListener(this::onCommandsRegister);
		MinecraftForge.EVENT_BUS.addListener(this::onTagsUpdated);
	}

	private static boolean checkDist(KiwiAnnotationData annotationData, String dist) throws IOException {
		try {
			ClassNode clazz = new ClassNode(Opcodes.ASM7);
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(annotationData.target().replace('.', '/') + ".class");
			final ClassReader classReader = new ClassReader(is);
			classReader.accept(clazz, 0);
			if (clazz.visibleAnnotations != null) {
				final String ONLYIN = Type.getDescriptor(OnlyIn.class);
				for (AnnotationNode node : clazz.visibleAnnotations) {
					if (node.values != null && ONLYIN.equals(node.desc)) {
						int i = node.values.indexOf("value");
						if (i != -1 && !node.values.get(i + 1).equals(dist)) {
							return false;
						}
					}
				}
			}
		} catch (Throwable e) {
			return false;
		}
		return true;
	}

	public static void preInit() {
		if (stage != LoadingStage.CONSTRUCTED) {
			return;
		}
		stage = LoadingStage.INITED;
		try {
			KiwiConfigManager.preload();
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			logger.fatal(MARKER, "Kiwi failed to start up. Please report this to developer!");
			logger.catching(e);
			return;
		}

		Set<ResourceLocation> disabledModules = Sets.newHashSet();
		conditions.forEach((k, v) -> {
			try {
				Class<?> clazz = Class.forName(k.target());
				String methodName = (String) k.data().get("method");
				List<String> values = (List<String>) k.data().get("value");
				if (values == null) {
					values = Arrays.asList(v);
				}
				List<ResourceLocation> ids = values.stream().map(s -> checkPrefix(s, v)).collect(Collectors.toList());
				for (ResourceLocation id : ids) {
					LoadingContext context = new LoadingContext(id);
					try {
						Boolean bl = (Boolean) MethodUtils.invokeExactStaticMethod(clazz, methodName, context);
						if (!bl) {
							disabledModules.add(id);
						}
					} catch (Exception e) {
						disabledModules.add(id);
						throw e;
					}
				}
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
				logger.error(MARKER, "Failed to access to LoadingCondition: {}", k);
				logger.catching(e);
			}
		});

		final Map<ResourceLocation, Info> infos = Maps.newHashMap();
		boolean checkDep = false;

		load:
		for (Entry<String, KiwiAnnotationData> entry : moduleData.entries()) {
			KiwiAnnotationData module = entry.getValue();
			String modid = entry.getKey();
			if (!Platform.isModLoaded(modid)) {
				continue;
			}

			String name = (String) module.data().get("value");
			if (Strings.isNullOrEmpty(name)) {
				name = "core";
			}

			ResourceLocation rl = new ResourceLocation(modid, name);
			if (disabledModules.contains(rl)) {
				if (KiwiConfigManager.modules.containsKey(rl)) { // module is optional
					continue;
				} else {
					throw new RuntimeException("Cannot load mandatory module: " + rl);
				}
			}
			if (KiwiConfigManager.modules.containsKey(rl) && !KiwiConfigManager.modules.get(rl).get()) {
				continue;
			}

			Info info = new Info(rl, module.target());

			String dependencies = (String) module.data().get("dependencies");
			/* off */
            List<String> rules = StringUtils.split(Strings.nullToEmpty(dependencies), ';').stream()
                    .filter(s -> !Strings.isNullOrEmpty(s))
                    .collect(Collectors.toList());
            /* on */

			for (String rule : rules) {
				if (rule.startsWith("@")) {
					info.moduleRules.add(Util.RL(rule.substring(1), modid));
					checkDep = true;
				} else if (!Platform.isModLoaded(rule)) {
					continue load;
				}
			}
			infos.put(rl, info);
		}

		List<ResourceLocation> moduleLoadingQueue = null;
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
			moduleLoadingQueue = TopologicalSort.topologicalSort(graph, null);
		} else {
			moduleLoadingQueue = ImmutableList.copyOf(infos.keySet());
		}

		for (ResourceLocation id : moduleLoadingQueue) {
			Info info = infos.get(id);
			ModContext context = ModContext.get(id.getNamespace());
			context.setActiveContainer();

			// Instantiate modules
			try {
				Class<?> clazz = Class.forName(info.className);
				AbstractModule instance = (AbstractModule) clazz.getDeclaredConstructor().newInstance();
				KiwiModules.add(id, instance, context);
			} catch (Exception e) {
				logger.error(MARKER, "Kiwi failed to initialize module class: {}", info.className);
				logger.catching(e);
				continue;
			}

			ModLoadingContext.get().setActiveContainer(null);
		}

		moduleData.clear();
		moduleData = null;
		defaultOptions.clear();
		defaultOptions = null;
		conditions.clear();
		conditions = null;

		Object2IntMap<Class<?>> counter = new Object2IntArrayMap<>();
		for (ModuleInfo info : KiwiModules.get()) {
			counter.clear();
			info.context.setActiveContainer();
			Subscriber subscriber = info.module.getClass().getDeclaredAnnotation(Subscriber.class);
			if (subscriber != null && ArrayUtils.contains(subscriber.side(), FMLEnvironment.dist)) {
				// processEvents(info.module);
				subscriber.value().bus().get().register(info.module);
			}

			boolean useOwnGroup = info.category == null;
			if (useOwnGroup) {
				Category group = info.module.getClass().getDeclaredAnnotation(Category.class);
				if (group != null) {
					String val = group.value();
					if (!val.isEmpty()) {
						useOwnGroup = false;
						CreativeModeTab itemGroup = getGroup(val);
						if (itemGroup != null) {
							info.category = itemGroup;
						}
					}
				}
			}

			String modid = info.module.uid.getNamespace();
			//			String name = info.module.uid.getPath();

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

				ResourceLocation regName;
				Name nameAnnotation = field.getAnnotation(Name.class);
				if (nameAnnotation != null) {
					regName = checkPrefix(nameAnnotation.value(), modid);
				} else {
					regName = checkPrefix(field.getName().toLowerCase(Locale.ENGLISH), modid);
				}

				if (field.getType() == info.module.getClass() && "instance".equals(regName.getPath()) && regName.getNamespace().equals(modid)) {
					try {
						field.set(null, info.module);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						logger.error(MARKER, "Kiwi failed to inject module instance to module class: {}", info.module.uid);
						logger.catching(e);
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
				if (useOwnGroup && info.category == null && o instanceof CreativeModeTab) {
					info.category = (CreativeModeTab) o;
				} else if (o instanceof Item.Properties) {
					tmpBuilder = (Item.Properties) o;
					tmpBuilderField = field;
					continue;
				}

				if (o instanceof KiwiGO) {
					o = ((KiwiGO<?>) o).create();
				}

				Object registry = registryLookup.findRegistry(o);
				if (registry != null) {
					if (o instanceof Block) {
						if (field.getAnnotation(NoItem.class) != null) {
							info.noItems.add((Block) o);
						}
						checkNoGroup(info, field, o);
						if (tmpBuilder != null) {
							info.blockItemBuilders.put((Block) o, tmpBuilder);
							try {
								tmpBuilderField.set(info.module, null);
							} catch (Exception e) {
								logger.error(MARKER, "Kiwi failed to clean used item builder: {}", tmpBuilderField);
								logger.catching(e);
							}
						}
					} else if (o instanceof Item) {
						checkNoGroup(info, field, o);
					}
					info.register(o, regName, registry, field);
				}

				tmpBuilder = null;
				tmpBuilderField = null;
			}
		}

		KiwiModules.fire(ModuleInfo::preInit);
		ModLoadingContext.get().setActiveContainer(null);
	}

	public static void registerRegistry(Registry<?> registry, Class<?> baseClass) {
		registryLookup.registries.put(baseClass, registry);
	}

	public static void registerRegistry(IForgeRegistry<?> registry, Class<?> baseClass) {
		registryLookup.registries.put(baseClass, registry);
	}

	//	@SuppressWarnings("rawtypes")
	private static <T> void registerRegistries() throws Exception {
		//		Map<String, Field> allFields = Maps.newHashMap();
		//		for (Field field : Registry.class.getFields()) {
		//			if (!Modifier.isStatic(field.getModifiers()) || !Modifier.isPublic(field.getModifiers())) {
		//				continue;
		//			}
		//			if (Registry.class.isAssignableFrom(field.getType())) {
		//				allFields.put(field.getName(), field);
		//			}
		//		}
		//
		//		StringBuilder sb = new StringBuilder();
		//		ClassNode clazz = new ClassNode(Opcodes.ASM7);
		//		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(Registry.class.getName().replace('.', '/') + ".class");
		//		final ClassReader classReader = new ClassReader(is);
		//		classReader.accept(clazz, 0);
		//
		//		Pattern pattern = Pattern.compile("<L([^<;]+?)[<;]");
		//		for (FieldNode field : clazz.fields) {
		//			if (allFields.containsKey(field.name)) {
		//				Matcher matcher = pattern.matcher(field.signature);
		//				if (!matcher.find()) {
		//					continue;
		//				}
		//				String className = matcher.group(1).replace('/', '.');
		//				Class<?> baseClass = Class.forName(className);
		//				sb.append("registerRegistry(Registry.%s, %s.class);\n".formatted(field.name, baseClass.getSimpleName()));
		//				registerRegistry((Registry) allFields.get(field.name).get(null), baseClass);
		//			}
		//		}
		//		System.out.println(sb);

		registerRegistry(Registry.GAME_EVENT, GameEvent.class);
		registerRegistry(ForgeRegistries.SOUND_EVENTS, SoundEvent.class);
		registerRegistry(ForgeRegistries.FLUIDS, Fluid.class);
		registerRegistry(ForgeRegistries.MOB_EFFECTS, MobEffect.class);
		registerRegistry(ForgeRegistries.BLOCKS, Block.class);
		registerRegistry(ForgeRegistries.ENCHANTMENTS, Enchantment.class);
		registerRegistry(ForgeRegistries.ENTITIES, EntityType.class);
		registerRegistry(ForgeRegistries.ITEMS, Item.class);
		registerRegistry(ForgeRegistries.POTIONS, Potion.class);
		registerRegistry(ForgeRegistries.PARTICLE_TYPES, ParticleType.class);
		registerRegistry(ForgeRegistries.BLOCK_ENTITIES, BlockEntityType.class);
		registerRegistry(ForgeRegistries.PAINTING_TYPES, Motive.class);
		registerRegistry(Registry.CUSTOM_STAT, ResourceLocation.class);
		registerRegistry(ForgeRegistries.CHUNK_STATUS, ChunkStatus.class);
		registerRegistry(Registry.RULE_TEST, RuleTestType.class);
		registerRegistry(Registry.POS_RULE_TEST, PosRuleTestType.class);
		registerRegistry(ForgeRegistries.CONTAINERS, MenuType.class);
		registerRegistry(Registry.RECIPE_TYPE, RecipeType.class);
		registerRegistry(ForgeRegistries.RECIPE_SERIALIZERS, RecipeSerializer.class);
		registerRegistry(ForgeRegistries.ATTRIBUTES, Attribute.class);
		registerRegistry(Registry.POSITION_SOURCE_TYPE, PositionSourceType.class);
		registerRegistry(ForgeRegistries.STAT_TYPES, StatType.class);
		registerRegistry(Registry.VILLAGER_TYPE, VillagerType.class);
		registerRegistry(ForgeRegistries.PROFESSIONS, VillagerProfession.class);
		registerRegistry(ForgeRegistries.POI_TYPES, PoiType.class);
		registerRegistry(ForgeRegistries.MEMORY_MODULE_TYPES, MemoryModuleType.class);
		registerRegistry(ForgeRegistries.SENSOR_TYPES, SensorType.class);
		registerRegistry(ForgeRegistries.SCHEDULES, Schedule.class);
		registerRegistry(ForgeRegistries.ACTIVITIES, Activity.class);
		registerRegistry(Registry.LOOT_POOL_ENTRY_TYPE, LootPoolEntryType.class);
		registerRegistry(Registry.LOOT_FUNCTION_TYPE, LootItemFunctionType.class);
		registerRegistry(Registry.LOOT_CONDITION_TYPE, LootItemConditionType.class);
		registerRegistry(Registry.LOOT_NUMBER_PROVIDER_TYPE, LootNumberProviderType.class);
		registerRegistry(Registry.LOOT_NBT_PROVIDER_TYPE, LootNbtProviderType.class);
		registerRegistry(Registry.LOOT_SCORE_PROVIDER_TYPE, LootScoreProviderType.class);
		registerRegistry(Registry.FLOAT_PROVIDER_TYPES, FloatProviderType.class);
		registerRegistry(Registry.INT_PROVIDER_TYPES, IntProviderType.class);
		registerRegistry(Registry.HEIGHT_PROVIDER_TYPES, HeightProviderType.class);
		registerRegistry(Registry.BLOCK_PREDICATE_TYPES, BlockPredicateType.class);
		registerRegistry(ForgeRegistries.WORLD_CARVERS, WorldCarver.class);
		registerRegistry(ForgeRegistries.FEATURES, Feature.class);
		registerRegistry(ForgeRegistries.STRUCTURE_FEATURES, StructureFeature.class);
		registerRegistry(Registry.STRUCTURE_PLACEMENT_TYPE, StructurePlacementType.class);
		registerRegistry(Registry.STRUCTURE_PIECE, StructurePieceType.class);
		registerRegistry(Registry.PLACEMENT_MODIFIERS, PlacementModifierType.class);
		registerRegistry(ForgeRegistries.BLOCK_STATE_PROVIDER_TYPES, BlockStateProviderType.class);
		registerRegistry(ForgeRegistries.FOLIAGE_PLACER_TYPES, FoliagePlacerType.class);
		registerRegistry(Registry.TRUNK_PLACER_TYPES, TrunkPlacerType.class);
		registerRegistry(ForgeRegistries.TREE_DECORATOR_TYPES, TreeDecoratorType.class);
		registerRegistry(Registry.FEATURE_SIZE_TYPES, FeatureSizeType.class);
		registerRegistry(Registry.BIOME_SOURCE, Codec.class);
		registerRegistry(Registry.CHUNK_GENERATOR, Codec.class);
		registerRegistry(Registry.CONDITION, Codec.class);
		registerRegistry(Registry.RULE, Codec.class);
		registerRegistry(Registry.DENSITY_FUNCTION_TYPES, Codec.class);
		registerRegistry(Registry.STRUCTURE_PROCESSOR, StructureProcessorType.class);
		registerRegistry(Registry.STRUCTURE_POOL_ELEMENT, StructurePoolElementType.class);
	}

	private static Map<String, CreativeModeTab> GROUP_CACHE = Maps.newHashMap();

	static CreativeModeTab getGroup(String path) {
		if (GROUP_CACHE == null) {
			return null;
		}
		return GROUP_CACHE.computeIfAbsent(path, $ -> {
			for (CreativeModeTab group : CreativeModeTab.TABS) {
				if (path.equals(group.getRecipeFolderName())) {
					return group;
				}
			}
			return null;
		});
	}

	private static void checkNoGroup(ModuleInfo info, Field field, Object o) {
		if (field.getAnnotation(NoCategory.class) != null) {
			info.noCategories.add(o);
		}
	}

	private void init(FMLCommonSetupEvent event) {
		KiwiConfigManager.refresh();
		InitEvent e = new InitEvent(event);
		KiwiModules.fire(m -> m.init(e));
		ModLoadingContext.get().setActiveContainer(null);

		BlockDefinition.registerFactory(SimpleBlockDefinition.Factory.INSTANCE);
	}

	private void clientInit(FMLClientSetupEvent event) {
		if (GROUP_CACHE == null) { // Not Enough Crashes mod can trigger init event
			return;
		}
		ClientInitEvent e = new ClientInitEvent(event);
		KiwiModules.fire(m -> m.clientInit(e));
		ModLoadingContext.get().setActiveContainer(null);
	}

	private void serverInit(ServerStartingEvent event) {
		if (GROUP_CACHE == null) { // Not Enough Crashes mod can trigger init event
			return;
		}
		ServerInitEvent e = new ServerInitEvent();
		KiwiModules.fire(m -> m.serverInit(e));
		event.getServer().getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(Scheduler::load, () -> Scheduler.INSTANCE, Scheduler.ID);
		ModLoadingContext.get().setActiveContainer(null);
	}

	private void onCommandsRegister(RegisterCommandsEvent event) {
		KiwiCommand.register(event.getDispatcher(), event.getEnvironment());
	}

	private void postInit(InterModProcessEvent event) {
		PostInitEvent e = new PostInitEvent(event);
		KiwiModules.fire(m -> m.postInit(e));
		ModLoadingContext.get().setActiveContainer(null);
		KiwiModules.clear();
	}

	private void loadComplete(FMLLoadCompleteEvent event) {
		GROUP_CACHE.clear();
		GROUP_CACHE = null;
	}

	public static boolean isLoaded(ResourceLocation module) {
		return KiwiModules.isLoaded(module);
	}

	private static ResourceLocation checkPrefix(String name, String defaultModid) {
		if (name.contains(":")) {
			return new ResourceLocation(name);
		} else {
			return new ResourceLocation(defaultModid, name);
		}
	}

	private static boolean tagsUpdated;

	private void onTagsUpdated(TagsUpdatedEvent event) {
		tagsUpdated = true;
	}

	/**
	 * @since 3.1.3
	 */
	public static boolean areTagsUpdated() {
		return tagsUpdated;
	}

	@OnlyIn(Dist.CLIENT)
	private void registerModelLoader(ModelRegistryEvent event) {
		ModelLoaderRegistry.registerLoader(Util.RL("kiwi:retexture"), RetextureModel.Loader.INSTANCE);
	}

}
