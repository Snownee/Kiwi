package snownee.kiwi;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.impl.metadata.AbstractModMetadata;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
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
import net.minecraft.world.entity.animal.CatVariant;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSourceType;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSizeType;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.rootplacers.RootPlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.StructureType;
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
import snownee.kiwi.KiwiModule.Category;
import snownee.kiwi.KiwiModule.Name;
import snownee.kiwi.KiwiModule.NoCategory;
import snownee.kiwi.KiwiModule.NoItem;
import snownee.kiwi.KiwiModule.RenderLayer.Layer;
import snownee.kiwi.KiwiModule.Skip;
import snownee.kiwi.block.def.BlockDefinition;
import snownee.kiwi.block.def.SimpleBlockDefinition;
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
import snownee.kiwi.util.Util;

@Mod(Kiwi.MODID)
public class Kiwi implements ModInitializer {
	public static final String MODID = "kiwi";
	public static final String NAME = "Kiwi";
	//	public static boolean CLOTH_CONFIG;

	public static Logger logger = LogManager.getLogger(Kiwi.NAME);
	static final Marker MARKER = MarkerManager.getMarker("Init");

	private static final class Info {
		@SuppressWarnings("unused")
		final ResourceLocation id;
		final String className;
		final List<ResourceLocation> moduleRules = Lists.newLinkedList();

		public Info(ResourceLocation id, String className) {
			this.id = id;
			this.className = className;
		}
	}

	private static Multimap<String, KiwiAnnotationData> moduleData = ArrayListMultimap.create();
	public static Map<ResourceLocation, Boolean> defaultOptions = Maps.newHashMap();
	private static Map<KiwiAnnotationData, String> conditions = Maps.newHashMap();
	private static RegistryLookup registryLookup = new RegistryLookup();

	@Override
	public void onInitialize() {
		//		CLOTH_CONFIG = Platform.isModLoaded("cloth-config");
		try {
			registerRegistries();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		Map<String, KiwiAnnotationData> classOptionalMap = Maps.newHashMap();
		String dist = Platform.isPhysicalClient() ? "client" : "server";
		List<String> mods = FabricLoader.getInstance().getAllMods().stream().map(ModContainer::getMetadata).filter($ -> !AbstractModMetadata.TYPE_BUILTIN.equals($.getType())).map(ModMetadata::getId).toList();
		for (String mod : mods) {
			if (mod.startsWith("fabric")) {
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
			//			if (CLOTH_CONFIG) {
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
			//			}
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
		CommandRegistrationCallback.EVENT.register(KiwiCommand::register);
		ServerLifecycleEvents.SERVER_STARTING.register(this::serverInit);
		ServerLifecycleEvents.SERVER_STOPPED.register($ -> currentServer = null);
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			return Util.onAttackEntity(player, world, hand, entity, hitResult);
		});
		if (Platform.isPhysicalClient()) {
			ClientLifecycleEvents.CLIENT_STARTED.register(this::clientInit);
		}
		preInit();
	}

	private static boolean checkDist(KiwiAnnotationData annotationData, String dist) {
		try {
			ClassNode clazz = new ClassNode(Opcodes.ASM7);
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(annotationData.target().replace('.', '/') + ".class");
			final ClassReader classReader = new ClassReader(is);
			classReader.accept(clazz, 0);
			if (clazz.visibleAnnotations != null) {
				final String ONLYIN = Type.getDescriptor(Environment.class);
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

	private void preInit() {
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
            List<String> rules = List.of(Strings.nullToEmpty(dependencies).split(";")).stream()
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
			//			List<Info> errorList = Lists.newLinkedList();
			//			for (Info i : infos.values()) {
			//				for (ResourceLocation id : i.moduleRules) {
			//					if (!infos.containsKey(id)) {
			//						errorList.add(i);
			//						break;
			//					}
			//				}
			//			}
			//			for (Info i : errorList) {
			//				IModInfo modInfo = ModList.get().getModContainerById(i.id.getNamespace()).get().getModInfo();
			//				String dependencies = org.apache.commons.lang3.StringUtils.join(i.moduleRules, ", ");
			//				ModLoader.get().addWarning(new ModLoadingWarning(modInfo, ModLoadingStage.ERROR, "msg.kiwi.no_dependencies", i.id, dependencies));
			//			}
			//			if (!errorList.isEmpty()) {
			//				return;
			//			}
			//			MutableGraph<ResourceLocation> graph = GraphBuilder.directed().allowsSelfLoops(false).expectedNodeCount(infos.size()).build();
			//			infos.keySet().forEach(graph::addNode);
			//			infos.values().forEach($ -> {
			//				$.moduleRules.forEach(r -> graph.putEdge(r, $.id));
			//			});
			//			list = TopologicalSort.topologicalSort(graph, null);
			moduleLoadingQueue = ImmutableList.copyOf(infos.keySet());
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
		}

		moduleData.clear();
		moduleData = null;
		defaultOptions.clear();
		defaultOptions = null;
		conditions.clear();
		conditions = null;

		Object2IntMap<ResourceKey<?>> counter = new Object2IntArrayMap<>();
		for (ModuleInfo info : KiwiModules.get()) {
			counter.clear();
			info.context.setActiveContainer();
			//			Subscriber subscriber = info.module.getClass().getDeclaredAnnotation(Subscriber.class);
			//			if (subscriber != null && ArrayUtils.contains(subscriber.side(), FMLEnvironment.dist)) {
			//				// processEvents(info.module);
			//				subscriber.value().bus().get().register(info.module);
			//			}

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

				Registry<?> registry = registryLookup.findRegistry(o);
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
					ResourceKey<?> superType = registry.key();
					int i = counter.getOrDefault(superType, 0);
					counter.put(superType, i + 1);
					info.register(o, regName, registry, field);
				}

				tmpBuilder = null;
				tmpBuilderField = null;
			}

			logger.info(MARKER, "Module [{}:{}] initialized", modid, name);
			for (ResourceKey<?> key : counter.keySet()) {
				String keyName = Util.trimRL(key.location());
				logger.info(MARKER, "    {}: {}", keyName, counter.getInt(key));
			}
		}

		KiwiModules.fire(ModuleInfo::preInit);
	}

	public static <T> void registerRegistry(Registry<? extends T> registry, Class<? extends T> baseClass) {
		Objects.requireNonNull(registryLookup);
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
		registerRegistry(Registry.SOUND_EVENT, SoundEvent.class);
		registerRegistry(Registry.FLUID, Fluid.class);
		registerRegistry(Registry.MOB_EFFECT, MobEffect.class);
		registerRegistry(Registry.BLOCK, Block.class);
		registerRegistry(Registry.ENCHANTMENT, Enchantment.class);
		registerRegistry(Registry.ENTITY_TYPE, EntityType.class);
		registerRegistry(Registry.ITEM, Item.class);
		registerRegistry(Registry.POTION, Potion.class);
		registerRegistry(Registry.PARTICLE_TYPE, ParticleType.class);
		registerRegistry(Registry.BLOCK_ENTITY_TYPE, BlockEntityType.class);
		registerRegistry(Registry.PAINTING_VARIANT, PaintingVariant.class);
		registerRegistry(Registry.CUSTOM_STAT, ResourceLocation.class);
		registerRegistry(Registry.CHUNK_STATUS, ChunkStatus.class);
		registerRegistry(Registry.RULE_TEST, RuleTestType.class);
		registerRegistry(Registry.POS_RULE_TEST, PosRuleTestType.class);
		registerRegistry(Registry.MENU, MenuType.class);
		registerRegistry(Registry.RECIPE_TYPE, RecipeType.class);
		registerRegistry(Registry.RECIPE_SERIALIZER, RecipeSerializer.class);
		registerRegistry(Registry.ATTRIBUTE, Attribute.class);
		registerRegistry(Registry.POSITION_SOURCE_TYPE, PositionSourceType.class);
		registerRegistry(Registry.COMMAND_ARGUMENT_TYPE, ArgumentTypeInfo.class);
		registerRegistry(Registry.STAT_TYPE, StatType.class);
		registerRegistry(Registry.VILLAGER_TYPE, VillagerType.class);
		registerRegistry(Registry.VILLAGER_PROFESSION, VillagerProfession.class);
		registerRegistry(Registry.POINT_OF_INTEREST_TYPE, PoiType.class);
		registerRegistry(Registry.MEMORY_MODULE_TYPE, MemoryModuleType.class);
		registerRegistry(Registry.SENSOR_TYPE, SensorType.class);
		registerRegistry(Registry.SCHEDULE, Schedule.class);
		registerRegistry(Registry.ACTIVITY, Activity.class);
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
		registerRegistry(Registry.CARVER, WorldCarver.class);
		registerRegistry(Registry.FEATURE, Feature.class);
		registerRegistry(Registry.STRUCTURE_PLACEMENT_TYPE, StructurePlacementType.class);
		registerRegistry(Registry.STRUCTURE_PIECE, StructurePieceType.class);
		registerRegistry(Registry.STRUCTURE_TYPES, StructureType.class);
		registerRegistry(Registry.PLACEMENT_MODIFIERS, PlacementModifierType.class);
		registerRegistry(Registry.BLOCKSTATE_PROVIDER_TYPES, BlockStateProviderType.class);
		registerRegistry(Registry.FOLIAGE_PLACER_TYPES, FoliagePlacerType.class);
		registerRegistry(Registry.TRUNK_PLACER_TYPES, TrunkPlacerType.class);
		registerRegistry(Registry.ROOT_PLACER_TYPES, RootPlacerType.class);
		registerRegistry(Registry.TREE_DECORATOR_TYPES, TreeDecoratorType.class);
		registerRegistry(Registry.FEATURE_SIZE_TYPES, FeatureSizeType.class);
		registerRegistry(Registry.STRUCTURE_PROCESSOR, StructureProcessorType.class);
		registerRegistry(Registry.STRUCTURE_POOL_ELEMENT, StructurePoolElementType.class);
		registerRegistry(Registry.CAT_VARIANT, CatVariant.class);
		registerRegistry(Registry.FROG_VARIANT, FrogVariant.class);
		registerRegistry(Registry.BANNER_PATTERN, BannerPattern.class);
		registerRegistry(Registry.INSTRUMENT, Instrument.class);
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

	private void init() {
		KiwiConfigManager.refresh();
		InitEvent e = new InitEvent();
		KiwiModules.fire(m -> m.init(e));
		BlockDefinition.registerFactory(SimpleBlockDefinition.Factory.INSTANCE);
	}

	@Environment(EnvType.CLIENT)
	private void clientInit(Minecraft mc) {
		if (GROUP_CACHE == null) { // Not Enough Crashes mod can trigger init event
			return;
		}
		init();
		ClientInitEvent e = new ClientInitEvent();
		KiwiModules.fire(m -> m.clientInit(e));
		postInit();
		loadComplete();
		Layer.CUTOUT.value = RenderType.cutout();
		Layer.CUTOUT_MIPPED.value = RenderType.cutoutMipped();
		Layer.TRANSLUCENT.value = RenderType.translucent();
	}

	public static MinecraftServer currentServer;

	private void serverInit(MinecraftServer server) {
		currentServer = server;
		if (GROUP_CACHE == null) { // Not Enough Crashes mod can trigger init event
			return;
		}
		if (server.isDedicatedServer()) {
			init();
		}
		ServerInitEvent e = new ServerInitEvent();
		KiwiModules.fire(m -> m.serverInit(e));
		if (server.isDedicatedServer()) {
			postInit();
			loadComplete();
		}
		//server.getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(Scheduler::load, () -> Scheduler.INSTANCE, Scheduler.ID);
	}

	private void postInit() {
		PostInitEvent e = new PostInitEvent();
		KiwiModules.fire(m -> m.postInit(e));
		KiwiModules.clear();
	}

	private void loadComplete() {
		GROUP_CACHE.clear();
		GROUP_CACHE = null;
		registryLookup = null;
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

	public static void onTagsUpdated() {
		tagsUpdated = true;
	}

	/**
	 * @since 3.1.3
	 */
	public static boolean areTagsUpdated() {
		return tagsUpdated;
	}

	//	@Environment(EnvType.CLIENT)
	//	private void registerModelLoader(ModelRegistryEvent event) {
	//		ModelLoaderRegistry.registerLoader(Util.RL("kiwi:retexture"), RetextureModel.Loader.INSTANCE);
	//	}

}
