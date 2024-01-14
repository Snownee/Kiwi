package snownee.kiwi;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;

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
import net.minecraft.core.registries.Registries;
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
import net.minecraft.world.item.CreativeModeTabs;
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
import snownee.kiwi.KiwiModule.RenderLayer.Layer;
import snownee.kiwi.block.def.BlockDefinition;
import snownee.kiwi.block.def.SimpleBlockDefinition;
import snownee.kiwi.command.KiwiCommand;
import snownee.kiwi.config.ConfigHandler;
import snownee.kiwi.config.KiwiConfig.ConfigType;
import snownee.kiwi.config.KiwiConfigManager;
import snownee.kiwi.loader.AnnotatedTypeLoader;
import snownee.kiwi.loader.KiwiConfiguration;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.loader.event.InitEvent;
import snownee.kiwi.loader.event.PostInitEvent;
import snownee.kiwi.network.Networking;
import snownee.kiwi.util.Util;

@Mod(Kiwi.ID)
public class Kiwi implements ModInitializer {
	public static final String ID = "kiwi";
	public static final RegistryLookup registryLookup = new RegistryLookup();
	static final Marker MARKER = MarkerFactory.getMarker("INIT");
	private static final Map<String, ResourceKey<CreativeModeTab>> GROUPS = Maps.newHashMap();
	public static final Logger LOGGER = LogUtils.getLogger();
	public static Map<ResourceLocation, Boolean> defaultOptions = Maps.newHashMap();
	public static MinecraftServer currentServer;
	private static Multimap<String, KiwiAnnotationData> moduleData = ArrayListMultimap.create();
	private static Map<KiwiAnnotationData, String> conditions = Maps.newHashMap();
	private static boolean tagsUpdated;

	private static boolean wrongDistribution(KiwiAnnotationData annotationData, String dist) {
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
							return true;
						}
					}
				}
			}
		} catch (Throwable e) {
			return true;
		}
		return false;
	}

	public static void registerRegistry(ResourceKey<? extends Registry<?>> registry, Class<?> baseClass) {
		Objects.requireNonNull(registryLookup);
		registryLookup.registries.put(baseClass, registry);
	}

	//	@SuppressWarnings("rawtypes")
	private static void registerRegistries() throws Exception {
		//		Map<String, Field> allFields = Maps.newHashMap();
		//		for (Field field : BuiltInRegistries.class.getFields()) {
		//			if (!Modifier.isStatic(field.getModifiers()) || !Modifier.isPublic(field.getModifiers())) {
		//				continue;
		//			}
		//			if (BuiltInRegistries.class.isAssignableFrom(field.getType())) {
		//				allFields.put(field.getName(), field);
		//			}
		//		}
		//
		//		StringBuilder sb = new StringBuilder();
		//		ClassNode clazz = new ClassNode(Opcodes.ASM7);
		//		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(BuiltInRegistries.class.getName().replace('.', '/') + ".class");
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
		//				sb.append("registerRegistry(Registries.%s, %s.class);\n".formatted(field.name, baseClass.getSimpleName()));
		//				registerRegistry((BuiltInRegistries) allFields.get(field.name).get(null), baseClass);
		//			}
		//		}
		//		System.out.println(sb);

		registerRegistry(Registries.GAME_EVENT, GameEvent.class);
		registerRegistry(Registries.SOUND_EVENT, SoundEvent.class);
		registerRegistry(Registries.FLUID, Fluid.class);
		registerRegistry(Registries.MOB_EFFECT, MobEffect.class);
		registerRegistry(Registries.BLOCK, Block.class);
		registerRegistry(Registries.ENCHANTMENT, Enchantment.class);
		registerRegistry(Registries.ENTITY_TYPE, EntityType.class);
		registerRegistry(Registries.ITEM, Item.class);
		registerRegistry(Registries.POTION, Potion.class);
		registerRegistry(Registries.PARTICLE_TYPE, ParticleType.class);
		registerRegistry(Registries.BLOCK_ENTITY_TYPE, BlockEntityType.class);
		registerRegistry(Registries.PAINTING_VARIANT, PaintingVariant.class);
		//registerRegistry(Registries.CUSTOM_STAT, ResourceLocation.class);
		registerRegistry(Registries.CHUNK_STATUS, ChunkStatus.class);
		registerRegistry(Registries.RULE_TEST, RuleTestType.class);
		registerRegistry(Registries.POS_RULE_TEST, PosRuleTestType.class);
		registerRegistry(Registries.MENU, MenuType.class);
		registerRegistry(Registries.RECIPE_TYPE, RecipeType.class);
		registerRegistry(Registries.RECIPE_SERIALIZER, RecipeSerializer.class);
		registerRegistry(Registries.ATTRIBUTE, Attribute.class);
		registerRegistry(Registries.POSITION_SOURCE_TYPE, PositionSourceType.class);
		registerRegistry(Registries.COMMAND_ARGUMENT_TYPE, ArgumentTypeInfo.class);
		registerRegistry(Registries.STAT_TYPE, StatType.class);
		registerRegistry(Registries.VILLAGER_TYPE, VillagerType.class);
		registerRegistry(Registries.VILLAGER_PROFESSION, VillagerProfession.class);
		registerRegistry(Registries.POINT_OF_INTEREST_TYPE, PoiType.class);
		registerRegistry(Registries.MEMORY_MODULE_TYPE, MemoryModuleType.class);
		registerRegistry(Registries.SENSOR_TYPE, SensorType.class);
		registerRegistry(Registries.SCHEDULE, Schedule.class);
		registerRegistry(Registries.ACTIVITY, Activity.class);
		registerRegistry(Registries.LOOT_POOL_ENTRY_TYPE, LootPoolEntryType.class);
		registerRegistry(Registries.LOOT_FUNCTION_TYPE, LootItemFunctionType.class);
		registerRegistry(Registries.LOOT_CONDITION_TYPE, LootItemConditionType.class);
		registerRegistry(Registries.LOOT_NUMBER_PROVIDER_TYPE, LootNumberProviderType.class);
		registerRegistry(Registries.LOOT_NBT_PROVIDER_TYPE, LootNbtProviderType.class);
		registerRegistry(Registries.LOOT_SCORE_PROVIDER_TYPE, LootScoreProviderType.class);
		registerRegistry(Registries.FLOAT_PROVIDER_TYPE, FloatProviderType.class);
		registerRegistry(Registries.INT_PROVIDER_TYPE, IntProviderType.class);
		registerRegistry(Registries.HEIGHT_PROVIDER_TYPE, HeightProviderType.class);
		registerRegistry(Registries.BLOCK_PREDICATE_TYPE, BlockPredicateType.class);
		registerRegistry(Registries.CARVER, WorldCarver.class);
		registerRegistry(Registries.FEATURE, Feature.class);
		registerRegistry(Registries.STRUCTURE_PLACEMENT, StructurePlacementType.class);
		registerRegistry(Registries.STRUCTURE_PIECE, StructurePieceType.class);
		registerRegistry(Registries.STRUCTURE_TYPE, StructureType.class);
		registerRegistry(Registries.PLACEMENT_MODIFIER_TYPE, PlacementModifierType.class);
		registerRegistry(Registries.BLOCK_STATE_PROVIDER_TYPE, BlockStateProviderType.class);
		registerRegistry(Registries.FOLIAGE_PLACER_TYPE, FoliagePlacerType.class);
		registerRegistry(Registries.TRUNK_PLACER_TYPE, TrunkPlacerType.class);
		registerRegistry(Registries.ROOT_PLACER_TYPE, RootPlacerType.class);
		registerRegistry(Registries.TREE_DECORATOR_TYPE, TreeDecoratorType.class);
		registerRegistry(Registries.FEATURE_SIZE_TYPE, FeatureSizeType.class);
		registerRegistry(Registries.STRUCTURE_PROCESSOR, StructureProcessorType.class);
		registerRegistry(Registries.STRUCTURE_POOL_ELEMENT, StructurePoolElementType.class);
		registerRegistry(Registries.CAT_VARIANT, CatVariant.class);
		registerRegistry(Registries.FROG_VARIANT, FrogVariant.class);
		registerRegistry(Registries.BANNER_PATTERN, BannerPattern.class);
		registerRegistry(Registries.INSTRUMENT, Instrument.class);
		registerRegistry(Registries.CREATIVE_MODE_TAB, CreativeModeTab.class);
	}

	public static void registerTab(String id, ResourceKey<CreativeModeTab> tab) {
		Validate.isTrue(!GROUPS.containsKey(id), "Already exists: %s", id);
		GROUPS.put(id, tab);
	}

	private static void registerTabs() {
		//TODO (1.21): use vanilla IDs
		registerTab(Categories.BUILDING_BLOCKS, CreativeModeTabs.BUILDING_BLOCKS);
		registerTab(Categories.COLORED_BLOCKS, CreativeModeTabs.COLORED_BLOCKS);
		registerTab(Categories.COMBAT, CreativeModeTabs.COMBAT);
		registerTab(Categories.FOOD_AND_DRINKS, CreativeModeTabs.FOOD_AND_DRINKS);
		registerTab(Categories.FUNCTIONAL_BLOCKS, CreativeModeTabs.FUNCTIONAL_BLOCKS);
		registerTab(Categories.INGREDIENTS, CreativeModeTabs.INGREDIENTS);
		registerTab(Categories.NATURAL_BLOCKS, CreativeModeTabs.NATURAL_BLOCKS);
		registerTab(Categories.OP_BLOCKS, CreativeModeTabs.OP_BLOCKS);
		registerTab(Categories.REDSTONE_BLOCKS, CreativeModeTabs.REDSTONE_BLOCKS);
		registerTab(Categories.SPAWN_EGGS, CreativeModeTabs.SPAWN_EGGS);
		registerTab(Categories.TOOLS_AND_UTILITIES, CreativeModeTabs.TOOLS_AND_UTILITIES);
	}

	@Nullable
	static ResourceKey<CreativeModeTab> getGroup(String path) {
		return GROUPS.get(path);
	}

	public static boolean isLoaded(ResourceLocation module) {
		return KiwiModules.isLoaded(module);
	}

	public static void onTagsUpdated() {
		tagsUpdated = true;
	}

	public static boolean areTagsUpdated() {
		return tagsUpdated;
	}

	@Override
	public void onInitialize() {
		try {
			registerRegistries();
			registerTabs();
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
				if (wrongDistribution(module, dist))
					continue;
				moduleData.put(mod, module);
			}
			for (KiwiAnnotationData optional : configuration.optionals) {
				if (wrongDistribution(optional, dist))
					continue;
				classOptionalMap.put(optional.target(), optional);
			}
			for (KiwiAnnotationData condition : configuration.conditions) {
				if (wrongDistribution(condition, dist))
					continue;
				conditions.put(condition, mod);
			}
			for (KiwiAnnotationData config : configuration.configs) {
				if (wrongDistribution(config, dist))
					continue;
				ConfigType type = null;
				try {
					type = ConfigType.valueOf((String) config.data().get("type"));
				} catch (Throwable e) {
				}
				type = type == null ? ConfigType.COMMON : type;
				if ((type != ConfigType.CLIENT || Platform.isPhysicalClient() || Platform.isDataGen())) {
					try {
						Class<?> clazz = Class.forName(config.target());
						String fileName = (String) config.data().get("value");
						boolean hasModules = type == ConfigType.COMMON && Strings.isNullOrEmpty(fileName);
						if (Strings.isNullOrEmpty(fileName)) {
							fileName = String.format("%s-%s", mod, type.extension());
						}
						new ConfigHandler(mod, fileName, type, clazz, hasModules);
					} catch (ClassNotFoundException e) {
						LOGGER.error(MARKER, "Failed to load config class {}", config.target());
					}
				}
			}
			for (KiwiAnnotationData packet : configuration.packets) {
				if (wrongDistribution(packet, dist))
					continue;
				Networking.processClass(packet.target(), mod);
			}
		}

		LOGGER.info(MARKER, "Processing " + moduleData.size() + " KiwiModule annotations");

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
		AttackEntityCallback.EVENT.register(Util::onAttackEntity);
		if (Platform.isPhysicalClient()) {
			Layer.CUTOUT.value = RenderType.cutout();
			Layer.CUTOUT_MIPPED.value = RenderType.cutoutMipped();
			Layer.TRANSLUCENT.value = RenderType.translucent();

			ClientLifecycleEvents.CLIENT_STARTED.register(this::clientInit);
		}
		preInit();
	}

	private void preInit() {
		Set<ResourceLocation> disabledModules = Sets.newHashSet();
		conditions.forEach((k, v) -> {
			try {
				Class<?> clazz = Class.forName(k.target());
				String methodName = (String) k.data().get("method");
				List<String> values = (List<String>) k.data().get("value");
				if (values == null) {
					values = List.of(v);
				}
				List<ResourceLocation> ids = values.stream().map(s -> Util.RL(s, v)).toList();
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
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
					 ClassNotFoundException e) {
				LOGGER.error(MARKER, "Failed to access to LoadingCondition: %s".formatted(k), e);
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
			List<String> rules = Stream.of(Strings.nullToEmpty(dependencies).split(";"))
					.filter(s -> !Strings.isNullOrEmpty(s))
					.toList();
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
				instantiateModule(id, clazz, context);
				if (Platform.isPhysicalClient()) {
					KiwiModule.ClientCompanion clientCompanion = clazz.getDeclaredAnnotation(KiwiModule.ClientCompanion.class);
					if (clientCompanion != null) {
						instantiateModule(id.withSuffix("_client"), clientCompanion.value(), context);
					}
				}
			} catch (Exception e) {
				LOGGER.error(MARKER, "Kiwi failed to initialize module class: %s".formatted(info.className), e);
			}
		}

		moduleData.clear();
		moduleData = null;
		defaultOptions.clear();
		defaultOptions = null;
		conditions.clear();
		conditions = null;

		KiwiModules.fire(KiwiModuleContainer::addRegistries);
		for (KiwiModuleContainer container : KiwiModules.get()) {
			container.loadGameObjects(registryLookup);
		}

		KiwiModules.ALL_USED_REGISTRIES.add(Registries.CREATIVE_MODE_TAB);
		KiwiModules.ALL_USED_REGISTRIES.add(Registries.ITEM);
		KiwiModules.fire(KiwiModuleContainer::addEntries);

		for (KiwiModuleContainer container : KiwiModules.get()) {
			LOGGER.info(MARKER, "Module [{}] initialized", container.module.uid);
			container.registries.registries.asMap().forEach((key, values) -> {
				LOGGER.info(MARKER, "\t\t{}: {}", Util.trimRL(key), values.size());
			});
		}
	}

	private static void instantiateModule(ResourceLocation id, Class<?> clazz, ModContext context) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		AbstractModule instance = (AbstractModule) clazz.getDeclaredConstructor().newInstance();
		KiwiModules.add(id, instance, context);
	}

	private void init() {
		KiwiConfigManager.refresh();
		InitEvent e = new InitEvent();
		KiwiModules.fire(m -> m.init(e));
		BlockDefinition.registerFactory(SimpleBlockDefinition.Factory.INSTANCE);
	}

	@Environment(EnvType.CLIENT)
	private void clientInit(Minecraft mc) {
		init();
		postInit();
		loadComplete();
	}

	private void serverInit(MinecraftServer server) {
		currentServer = server;
		if (server.isDedicatedServer()) {
			init();
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
		registryLookup.cache.invalidateAll();
	}

	private record Info(ResourceLocation id, String className, List<ResourceLocation> moduleRules) {
		Info(ResourceLocation id, String className) {
			this(id, className, Lists.newArrayList());
		}
	}

	//	@Environment(EnvType.CLIENT)
	//	private void registerModelLoader(ModelRegistryEvent event) {
	//		ModelLoaderRegistry.registerLoader(Util.RL("kiwi:retexture"), RetextureModel.Loader.INSTANCE);
	//	}

}
