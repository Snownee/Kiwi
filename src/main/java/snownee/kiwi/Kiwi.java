package snownee.kiwi;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
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
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.mojang.logging.LogUtils;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.fml.loading.toposort.TopologicalSort;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.crafting.FluidIngredientType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.holdersets.HolderSetType;
import net.neoforged.neoforgespi.language.IModInfo;
import snownee.kiwi.build.KiwiMetadata;
import snownee.kiwi.build.KiwiMetadataParser;
import snownee.kiwi.command.KiwiCommand;
import snownee.kiwi.config.ConfigHandler;
import snownee.kiwi.config.KiwiConfig.ConfigType;
import snownee.kiwi.config.KiwiConfigManager;
import snownee.kiwi.config.NeoClothConfigIntegration;
import snownee.kiwi.loader.ClientInitializer;
import snownee.kiwi.loader.KiwiMetadataLoader;
import snownee.kiwi.loader.NeoDevEnvMetadataLoader;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.loader.event.InitEvent;
import snownee.kiwi.loader.event.PostInitEvent;
import snownee.kiwi.network.KNetworking;
import snownee.kiwi.util.KUtil;

@Mod(Kiwi.ID)
public class Kiwi {
	public static final String ID = "kiwi";
	public static final RegistryLookup registryLookup = new RegistryLookup();
	static final Marker MARKER = MarkerFactory.getMarker("INIT");
	public static final Logger LOGGER = LogUtils.getLogger();
	public static Map<ResourceLocation, Boolean> defaultOptions = Maps.newHashMap();
	private static Multimap<String, KiwiAnnotationData> moduleData = ArrayListMultimap.create();
	private static Map<KiwiAnnotationData, String> conditions = Maps.newHashMap();
	private static LoadingStage stage = LoadingStage.UNINITED;
	private static final Map<String, ResourceKey<CreativeModeTab>> GROUPS = Maps.newHashMap();
	public static boolean enableDataModule;

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(ID, path);
	}

	private static boolean shouldLoad(KiwiAnnotationData annotationData, String dist) {
		try {
			String target = annotationData.getTarget();
			if (Platform.isProduction() && target.startsWith("snownee.kiwi.test.")) {
				return false;
			}
			ClassNode clazz = new ClassNode(Opcodes.ASM7);
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(
					target.replace('.', '/') + ".class");
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
			return true;
		} catch (Throwable e) {
			return false;
		}
	}

	public Kiwi(IEventBus modEventBus) throws Exception {
		if (stage != LoadingStage.UNINITED) {
			return;
		}
		stage = LoadingStage.CONSTRUCTING;
		try {
			registerRegistries();
			registerTabs();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		Map<String, KiwiAnnotationData> classOptionalMap = Maps.newHashMap();
		String dist = Platform.isPhysicalClient() ? "client" : "server";
		List<String> mods = ModList.get().getMods().stream().map(IModInfo::getModId).toList();
		KiwiMetadataParser metadataParser = new KiwiMetadataParser();
		for (String mod : mods) {
			if ("neoforge".equals(mod)) {
				continue;
			}
			boolean yamlLoader = Platform.isProduction() || System.getProperties().containsKey("kiwi.disableDevEnvMetadataLoader");
			Function<KiwiMetadataParser, KiwiMetadata> loader = yamlLoader ? new KiwiMetadataLoader(mod) : new NeoDevEnvMetadataLoader(mod);
			KiwiMetadata metadata = loader.apply(metadataParser);
			if (metadata == null) {
				continue;
			}

			if (!metadata.clientOnly()) {
				enableDataModule();
			}
			for (KiwiAnnotationData module : metadata.get("modules")) {
				if (shouldLoad(module, dist)) {
					moduleData.put(mod, module);
				}
			}
			for (KiwiAnnotationData optional : metadata.get("optionals")) {
				if (shouldLoad(optional, dist)) {
					classOptionalMap.put(optional.getTarget(), optional);
				}
			}
			for (KiwiAnnotationData condition : metadata.get("conditions")) {
				if (shouldLoad(condition, dist)) {
					conditions.put(condition, mod);
				}
			}
			for (KiwiAnnotationData config : metadata.get("configs")) {
				if (!shouldLoad(config, dist)) {
					continue;
				}
				ConfigType type = null;
				try {
					type = ConfigType.valueOf((String) config.getData().get("type"));
				} catch (Throwable ignored) {
				}
				type = type == null ? ConfigType.COMMON : type;
				if ((type != ConfigType.CLIENT || Platform.isPhysicalClient() || Platform.isDataGen())) {
					try {
						Class<?> clazz = Class.forName(config.getTarget());
						String fileName = (String) config.getData().get("value");
						boolean hasModules = type == ConfigType.COMMON && Strings.isNullOrEmpty(fileName);
						if (Strings.isNullOrEmpty(fileName)) {
							fileName = String.format("%s-%s", mod, type.extension());
						}
						new ConfigHandler(mod, fileName, type, clazz, hasModules);
					} catch (ClassNotFoundException e) {
						LOGGER.error(MARKER, "Failed to load config class {}", config.getTarget());
					}
				}
			}
			KNetworking networking = new KNetworking(modEventBus);
			for (KiwiAnnotationData packet : metadata.get("packets")) {
				if (shouldLoad(packet, dist)) {
					networking.processClass(packet);
				}
			}
		}

		LOGGER.info(MARKER, "Processing " + moduleData.size() + " KiwiModule annotations");

		for (Entry<String, KiwiAnnotationData> entry : moduleData.entries()) {
			KiwiAnnotationData optional = classOptionalMap.get(entry.getValue().getTarget());
			if (optional != null) {
				String modid = entry.getKey();
				if (!Platform.isModLoaded(modid)) {
					continue;
				}

				String name = (String) entry.getValue().getData().get("value");
				if (Strings.isNullOrEmpty(name)) {
					name = "core";
				}

				Boolean defaultEnabled = (Boolean) optional.getData().get("defaultEnabled");
				if (defaultEnabled == null) {
					defaultEnabled = Boolean.TRUE;
				}
				defaultOptions.put(ResourceLocation.fromNamespaceAndPath(modid, name), defaultEnabled);
			}
		}

		KiwiConfigManager.init();
		if (Platform.isPhysicalClient() && Platform.isModLoaded("cloth_config")) {
			NeoClothConfigIntegration.init();
		}
		modEventBus.addListener(this::init);
		modEventBus.addListener(this::postInit);
		modEventBus.addListener(this::loadComplete);
//		if (Platform.isModLoaded("fabric_api")) {
//			modEventBus.addListener(this::gatherData);
//		}
		//modEventBus.register(KiwiModules.class); // Cannot register without at least one event listener
		if (Platform.isPhysicalClient()) {
			RenderLayerEnum.CUTOUT.value = ChunkSectionLayer.CUTOUT;
			RenderLayerEnum.CUTOUT_MIPPED.value = ChunkSectionLayer.CUTOUT_MIPPED;
			RenderLayerEnum.TRANSLUCENT.value = ChunkSectionLayer.TRANSLUCENT;

			NeoForge.EVENT_BUS.register(ClientInitializer.class);
		}
		NeoForge.EVENT_BUS.addListener(this::onCommandsRegister);
		NeoForge.EVENT_BUS.addListener(this::onAttachEntity);
		stage = LoadingStage.CONSTRUCTED;
	}

	public static void preInit() {
		if (stage != LoadingStage.CONSTRUCTED) {
			return;
		}

		Set<ResourceLocation> disabledModules = Sets.newHashSet();
		conditions.forEach((k, v) -> {
			try {
				Class<?> clazz = Class.forName(k.getTarget());
				String methodName = (String) k.getData().get("method");
				List<String> values = (List<String>) k.getData().get("value");
				if (values == null) {
					values = List.of(v);
				}
				List<ResourceLocation> ids = values.stream().map(s -> KUtil.RL(s, v)).toList();
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

			String name = (String) module.getData().get("value");
			if (Strings.isNullOrEmpty(name)) {
				name = "core";
			}

			ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(modid, name);
			if (disabledModules.contains(rl)) {
				continue;
			}
			if (KiwiConfigManager.modules.containsKey(rl) && !KiwiConfigManager.modules.get(rl).get()) {
				continue;
			}

			Info info = new Info(rl, module.getTarget());

			String dependencies = (String) module.getData().get("dependencies");
			/* off */
			List<String> rules = Stream.of(Strings.nullToEmpty(dependencies).split(";"))
					.filter(s -> !Strings.isNullOrEmpty(s))
					.toList();
			/* on */

			for (String rule : rules) {
				if (rule.startsWith("@")) {
					info.moduleRules.add(KUtil.RL(rule.substring(1), modid));
					checkDep = true;
				} else if (!Platform.isModLoaded(rule)) {
					continue load;
				}
			}
			infos.put(rl, info);
		}

		List<ResourceLocation> moduleLoadingQueue;
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
				String dependencies = org.apache.commons.lang3.StringUtils.join(i.moduleRules, ", ");
				ModLoader.addLoadingIssue(ModLoadingIssue.error(
						"msg.kiwi.no_dependencies",
						i.id,
						dependencies));
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
				instantiateModule(id, clazz, context);
				if (Platform.isPhysicalClient()) {
					KiwiModule.ClientCompanion clientCompanion = clazz.getDeclaredAnnotation(KiwiModule.ClientCompanion.class);
					if (clientCompanion != null) {
						instantiateModule(id.withSuffix("_client"), clientCompanion.value(), context);
					}
				}
			} catch (Exception e) {
				LOGGER.error(MARKER, "Kiwi failed to initialize module class: %s".formatted(info.className), e);
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

		KiwiModules.fire(KiwiModuleContainer::addRegistries);
		ModLoadingContext.get().setActiveContainer(null);

		for (KiwiModuleContainer container : KiwiModules.get()) {
			container.loadGameObjects();
		}

		KiwiModules.ALL_USED_REGISTRIES.add(Registries.CREATIVE_MODE_TAB);
		KiwiModules.ALL_USED_REGISTRIES.add(Registries.ITEM);
		KiwiModules.fire(KiwiModuleContainer::addEntries);
		ModLoadingContext.get().setActiveContainer(null);

		List<String> entries = Lists.newArrayList();
		for (KiwiModuleContainer container : KiwiModules.get()) {
			ResourceLocation uid = Objects.requireNonNull(container.module.uid);
			if (ID.equals(uid.getNamespace()) && uid.getPath().startsWith("contributors")) {
				continue;
			}
			LOGGER.info(MARKER, "Module [{}] initialized", uid);
			container.registries.registries.asMap().forEach((key, values) -> {
				if (!values.isEmpty()) {
					entries.add("%s: %s".formatted(KUtil.trimRL(key), values.size()));
				}
			});
			if (!entries.isEmpty()) {
				LOGGER.info(MARKER, "\t\t" + String.join(", ", entries));
				entries.clear();
			}
		}

		stage = LoadingStage.INITED;
	}

	private static void instantiateModule(
			ResourceLocation id,
			Class<?> clazz,
			ModContext context) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		AbstractModule instance = (AbstractModule) clazz.getDeclaredConstructor().newInstance();
		KiwiModules.add(id, instance, context);
	}

	public static void registerRegistry(ResourceKey<? extends Registry<?>> registry, Class<?> baseClass) {
		Objects.requireNonNull(registryLookup);
		if (registryLookup.registries.put(baseClass, registry) != null) {
			LOGGER.warn("Registry {} already registered for {}", registry, baseClass);
		}
	}

	private static void registerRegistries() {
//		StringBuilder sb = new StringBuilder();
//		for (Field field : BuiltInRegistries.class.getFields()) {
//			if (!Modifier.isStatic(field.getModifiers()) || !Modifier.isPublic(field.getModifiers())) {
//				continue;
//			}
//			if (!Registry.class.isAssignableFrom(field.getType())) {
//				continue;
//			}
//			if (!(field.getGenericType() instanceof ParameterizedType parameterizedType)) {
//				continue;
//			}
//			Class<?> baseClass = TypeToken.of(parameterizedType.getActualTypeArguments()[0]).getRawType();
//			if (baseClass == ResourceLocation.class || baseClass == MapCodec.class || baseClass == DataComponentType.class ||
//					baseClass == Consumer.class || baseClass == Registry.class) {
//				continue;
//			}
//
//			// Example: net.minecraft.world.item.consume_effects.ConsumeEffect$Type -> ConsumeEffect.Type
//			sb.append("registerRegistry(Registries.%s, %s.class);\n".formatted(field.getName(), baseClass.getName().replace('$', '.')));
//			registerRegistry(((Registry) field.get(null)).key(), baseClass);
//		}
//		LOGGER.info(sb.toString());

		registerRegistry(Registries.GAME_EVENT, net.minecraft.world.level.gameevent.GameEvent.class);
		registerRegistry(Registries.SOUND_EVENT, net.minecraft.sounds.SoundEvent.class);
		registerRegistry(Registries.FLUID, net.minecraft.world.level.material.Fluid.class);
		registerRegistry(Registries.MOB_EFFECT, net.minecraft.world.effect.MobEffect.class);
		registerRegistry(Registries.BLOCK, net.minecraft.world.level.block.Block.class);
		registerRegistry(Registries.ENTITY_TYPE, net.minecraft.world.entity.EntityType.class);
		registerRegistry(Registries.ITEM, net.minecraft.world.item.Item.class);
		registerRegistry(Registries.POTION, net.minecraft.world.item.alchemy.Potion.class);
		registerRegistry(Registries.PARTICLE_TYPE, net.minecraft.core.particles.ParticleType.class);
		registerRegistry(Registries.BLOCK_ENTITY_TYPE, net.minecraft.world.level.block.entity.BlockEntityType.class);
		registerRegistry(Registries.CHUNK_STATUS, net.minecraft.world.level.chunk.status.ChunkStatus.class);
		registerRegistry(Registries.RULE_TEST, net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType.class);
		registerRegistry(
				Registries.RULE_BLOCK_ENTITY_MODIFIER,
				net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.RuleBlockEntityModifierType.class);
		registerRegistry(Registries.POS_RULE_TEST, net.minecraft.world.level.levelgen.structure.templatesystem.PosRuleTestType.class);
		registerRegistry(Registries.MENU, net.minecraft.world.inventory.MenuType.class);
		registerRegistry(Registries.RECIPE_TYPE, net.minecraft.world.item.crafting.RecipeType.class);
		registerRegistry(Registries.RECIPE_SERIALIZER, net.minecraft.world.item.crafting.RecipeSerializer.class);
		registerRegistry(Registries.ATTRIBUTE, net.minecraft.world.entity.ai.attributes.Attribute.class);
		registerRegistry(Registries.POSITION_SOURCE_TYPE, net.minecraft.world.level.gameevent.PositionSourceType.class);
		registerRegistry(Registries.COMMAND_ARGUMENT_TYPE, net.minecraft.commands.synchronization.ArgumentTypeInfo.class);
		registerRegistry(Registries.STAT_TYPE, net.minecraft.stats.StatType.class);
		registerRegistry(Registries.VILLAGER_TYPE, net.minecraft.world.entity.npc.VillagerType.class);
		registerRegistry(Registries.VILLAGER_PROFESSION, net.minecraft.world.entity.npc.VillagerProfession.class);
		registerRegistry(Registries.POINT_OF_INTEREST_TYPE, net.minecraft.world.entity.ai.village.poi.PoiType.class);
		registerRegistry(Registries.MEMORY_MODULE_TYPE, net.minecraft.world.entity.ai.memory.MemoryModuleType.class);
		registerRegistry(Registries.SENSOR_TYPE, net.minecraft.world.entity.ai.sensing.SensorType.class);
		registerRegistry(Registries.SCHEDULE, net.minecraft.world.entity.schedule.Schedule.class);
		registerRegistry(Registries.ACTIVITY, net.minecraft.world.entity.schedule.Activity.class);
		registerRegistry(Registries.LOOT_POOL_ENTRY_TYPE, net.minecraft.world.level.storage.loot.entries.LootPoolEntryType.class);
		registerRegistry(Registries.LOOT_FUNCTION_TYPE, net.minecraft.world.level.storage.loot.functions.LootItemFunctionType.class);
		registerRegistry(Registries.LOOT_CONDITION_TYPE, net.minecraft.world.level.storage.loot.predicates.LootItemConditionType.class);
		registerRegistry(
				Registries.LOOT_NUMBER_PROVIDER_TYPE,
				net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType.class);
		registerRegistry(Registries.LOOT_NBT_PROVIDER_TYPE, net.minecraft.world.level.storage.loot.providers.nbt.LootNbtProviderType.class);
		registerRegistry(
				Registries.LOOT_SCORE_PROVIDER_TYPE,
				net.minecraft.world.level.storage.loot.providers.score.LootScoreProviderType.class);
		registerRegistry(Registries.FLOAT_PROVIDER_TYPE, net.minecraft.util.valueproviders.FloatProviderType.class);
		registerRegistry(Registries.INT_PROVIDER_TYPE, net.minecraft.util.valueproviders.IntProviderType.class);
		registerRegistry(Registries.HEIGHT_PROVIDER_TYPE, net.minecraft.world.level.levelgen.heightproviders.HeightProviderType.class);
		registerRegistry(Registries.BLOCK_PREDICATE_TYPE, net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType.class);
		registerRegistry(Registries.CARVER, net.minecraft.world.level.levelgen.carver.WorldCarver.class);
		registerRegistry(Registries.FEATURE, net.minecraft.world.level.levelgen.feature.Feature.class);
		registerRegistry(
				Registries.STRUCTURE_PLACEMENT,
				net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType.class);
		registerRegistry(Registries.STRUCTURE_PIECE, net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType.class);
		registerRegistry(Registries.STRUCTURE_TYPE, net.minecraft.world.level.levelgen.structure.StructureType.class);
		registerRegistry(Registries.PLACEMENT_MODIFIER_TYPE, net.minecraft.world.level.levelgen.placement.PlacementModifierType.class);
		registerRegistry(
				Registries.BLOCK_STATE_PROVIDER_TYPE,
				net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType.class);
		registerRegistry(Registries.FOLIAGE_PLACER_TYPE, net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType.class);
		registerRegistry(Registries.TRUNK_PLACER_TYPE, net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType.class);
		registerRegistry(Registries.ROOT_PLACER_TYPE, net.minecraft.world.level.levelgen.feature.rootplacers.RootPlacerType.class);
		registerRegistry(Registries.TREE_DECORATOR_TYPE, net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType.class);
		registerRegistry(Registries.FEATURE_SIZE_TYPE, net.minecraft.world.level.levelgen.feature.featuresize.FeatureSizeType.class);
		registerRegistry(
				Registries.STRUCTURE_PROCESSOR,
				net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType.class);
		registerRegistry(
				Registries.STRUCTURE_POOL_ELEMENT,
				net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType.class);
		registerRegistry(Registries.DECORATED_POT_PATTERN, net.minecraft.world.level.block.entity.DecoratedPotPattern.class);
		registerRegistry(Registries.CREATIVE_MODE_TAB, net.minecraft.world.item.CreativeModeTab.class);
		registerRegistry(Registries.TRIGGER_TYPE, net.minecraft.advancements.CriterionTrigger.class);
		registerRegistry(Registries.NUMBER_FORMAT_TYPE, net.minecraft.network.chat.numbers.NumberFormatType.class);
		registerRegistry(
				Registries.DATA_COMPONENT_PREDICATE_TYPE,
				net.minecraft.core.component.predicates.DataComponentPredicate.Type.class);
		registerRegistry(Registries.MAP_DECORATION_TYPE, net.minecraft.world.level.saveddata.maps.MapDecorationType.class);
		registerRegistry(Registries.CONSUME_EFFECT_TYPE, net.minecraft.world.item.consume_effects.ConsumeEffect.Type.class);
		registerRegistry(Registries.RECIPE_DISPLAY, net.minecraft.world.item.crafting.display.RecipeDisplay.Type.class);
		registerRegistry(Registries.SLOT_DISPLAY, net.minecraft.world.item.crafting.display.SlotDisplay.Type.class);
		registerRegistry(Registries.RECIPE_BOOK_CATEGORY, net.minecraft.world.item.crafting.RecipeBookCategory.class);
		registerRegistry(Registries.TICKET_TYPE, net.minecraft.server.level.TicketType.class);

		registerRegistry(NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, EntityDataSerializer.class);
		registerRegistry(NeoForgeRegistries.Keys.FLUID_TYPES, FluidType.class);
		registerRegistry(NeoForgeRegistries.Keys.HOLDER_SET_TYPES, HolderSetType.class);
		registerRegistry(NeoForgeRegistries.Keys.INGREDIENT_TYPES, IngredientType.class);
		registerRegistry(NeoForgeRegistries.Keys.FLUID_INGREDIENT_TYPES, FluidIngredientType.class);
		registerRegistry(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, AttachmentType.class);
	}

	public static void registerTab(String id, ResourceKey<CreativeModeTab> tab) {
		Validate.isTrue(!GROUPS.containsKey(id), "Already exists: %s", id);
		GROUPS.put(id, tab);
	}

	private static void registerTabs() {
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

	public static void enableDataModule() {
		enableDataModule = true;
	}

	private void init(FMLCommonSetupEvent event) {
		KiwiConfigManager.refresh();
		InitEvent e = new InitEvent(event);
		KiwiModules.fire(m -> m.init(e));
		ModLoadingContext.get().setActiveContainer(null);
	}

	private void onCommandsRegister(RegisterCommandsEvent event) {
		KiwiCommand.register(event.getDispatcher());
	}

	private void postInit(InterModProcessEvent event) {
		PostInitEvent e = new PostInitEvent(event);
		KiwiModules.fire(m -> m.postInit(e));
		ModLoadingContext.get().setActiveContainer(null);
		KiwiModules.clear();
	}

	private void loadComplete(FMLLoadCompleteEvent event) {
		registryLookup.cache.invalidateAll();
	}

	private void onAttachEntity(AttackEntityEvent event) {
		KUtil.onAttackEntity(event.getEntity(), event.getEntity().level(), InteractionHand.MAIN_HAND, event.getTarget(), null);
	}

	private enum LoadingStage {
		UNINITED, CONSTRUCTING, CONSTRUCTED, INITED;
	}

	private static final class Info {
		final ResourceLocation id;
		final String className;
		final List<ResourceLocation> moduleRules = Lists.newLinkedList();

		public Info(ResourceLocation id, String className) {
			this.id = id;
			this.className = className;
		}
	}

}
