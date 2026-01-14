package snownee.kiwi.customization;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.LoadingContext;
import snownee.kiwi.customization.block.BlockFundamentals;
import snownee.kiwi.customization.block.GlassType;
import snownee.kiwi.customization.block.KBlockSettings;
import snownee.kiwi.customization.block.behavior.BlockBehaviorRegistry;
import snownee.kiwi.customization.block.behavior.SitManager;
import snownee.kiwi.customization.block.component.KBlockComponent;
import snownee.kiwi.customization.block.family.BlockFamilies;
import snownee.kiwi.customization.block.loader.KBlockTemplate;
import snownee.kiwi.customization.block.soundtype.DeferredSoundType;
import snownee.kiwi.customization.block.soundtype.SoundTypes;
import snownee.kiwi.customization.block.toolmaterial.ToolMaterials;
import snownee.kiwi.customization.builder.BuilderRule;
import snownee.kiwi.customization.builder.BuilderRules;
import snownee.kiwi.customization.item.ItemFundamentals;
import snownee.kiwi.customization.item.loader.KCreativeTab;
import snownee.kiwi.customization.item.loader.KItemTemplate;
import snownee.kiwi.customization.placement.PlacementSystem;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.util.ClientProxy;
import snownee.kiwi.util.resource.OneTimeLoader;
import snownee.kiwi.util.resource.RequiredFolderRepositorySource;

public final class CustomizationHooks {
	private static final Set<String> blockNamespaces = Sets.newLinkedHashSet();
	private static final Set<String> lenientBETypeNamespaces = Sets.newHashSet();
	private static boolean enabled = true;
	public static boolean kswitch = Platform.isModLoaded("kswitch") || !Platform.isProduction();
	private static @Nullable GlassType clearGlassType;

	private CustomizationHooks() {
	}

	/// a custom implementation of the {@link Block#shouldRenderFace(BlockState, BlockState, Direction)}
	private static final int CACHE_SIZE = 512;
	private static final ThreadLocal<Object2ByteLinkedOpenHashMap<BlockStatePairKey>> OCCLUSION_CACHE = ThreadLocal.withInitial(() -> {
		Object2ByteLinkedOpenHashMap<BlockStatePairKey> object2bytelinkedopenhashmap = new Object2ByteLinkedOpenHashMap<>(
				CACHE_SIZE,
				0.25F) {
			@Override
			protected void rehash(int needed) {
			}
		};
		object2bytelinkedopenhashmap.defaultReturnValue((byte) 127);
		return object2bytelinkedopenhashmap;
	});

	public static boolean isEnabled() {
		return enabled;
	}

	public static boolean skipGlassRendering(BlockState pState, BlockState pAdjacentBlockState, Direction pDirection) {
		if (KBlockSettings.of(pState.getBlock()) == null && KBlockSettings.of(pAdjacentBlockState.getBlock()) == null) {
			return false;
		}
		GlassType glassType = getGlassType(pState);
		if (glassType == null || !glassType.skipRendering()) {
			return false;
		}
		if (!pState.is(pAdjacentBlockState.getBlock()) && glassType != getGlassType(pAdjacentBlockState)) {
			return false;
		}
		BlockStatePairKey key = new BlockStatePairKey(pState, pAdjacentBlockState, pDirection);
		Object2ByteLinkedOpenHashMap<BlockStatePairKey> map = OCCLUSION_CACHE.get();
		byte b0 = map.getAndMoveToFirst(key);
		if (b0 != 127) {
			return b0 == 0;
		}
		VoxelShape shape1 = KBlockSettings.getGlassFaceShape(pState, pDirection);
		if (shape1.isEmpty()) {
			return true;
		}
		VoxelShape shape2 = KBlockSettings.getGlassFaceShape(pAdjacentBlockState, pDirection.getOpposite());
		boolean flag = Shapes.joinIsNotEmpty(shape1, shape2, BooleanOp.ONLY_FIRST);
		if (map.size() == CACHE_SIZE) {
			map.removeLastByte();
		}
		map.putAndMoveToFirst(key, (byte) (flag ? 1 : 0));
		return !flag;
	}

	@Nullable
	public static GlassType getGlassType(BlockState blockState) {
		KBlockSettings settings = KBlockSettings.of(blockState.getBlock());
		if (settings != null && settings.glassType != null) {
			return settings.glassType;
		}
		if (isColorlessGlass(blockState)) {
			return clearGlassType;
		}
		return null;
	}

	public static void init() {
		enabled = CustomizationServiceFinder.shouldEnable(FabricLoader.getInstance().getAllMods());
		if (!isEnabled()) {
			return;
		}
		Kiwi.LOGGER.info("Kiwi Customization is enabled");
		CustomizationRegistries.BLOCK_COMPONENT = FabricRegistryBuilder.create(CustomizationRegistries.BLOCK_COMPONENT_KEY)
				.buildAndRegister();
		Kiwi.registerRegistry(CustomizationRegistries.BLOCK_COMPONENT_KEY, KBlockComponent.Type.class);
		CustomizationRegistries.BLOCK_TEMPLATE = FabricRegistryBuilder.create(CustomizationRegistries.BLOCK_TEMPLATE_KEY)
				.buildAndRegister();
		Kiwi.registerRegistry(CustomizationRegistries.BLOCK_TEMPLATE_KEY, KBlockTemplate.Type.class);
		CustomizationRegistries.ITEM_TEMPLATE = FabricRegistryBuilder.create(CustomizationRegistries.ITEM_TEMPLATE_KEY)
				.buildAndRegister();
		Kiwi.registerRegistry(CustomizationRegistries.ITEM_TEMPLATE_KEY, KItemTemplate.Type.class);
		CustomizationRegistries.BUILDER_RULE = FabricRegistryBuilder.create(CustomizationRegistries.BUILDER_RULE_KEY)
				.buildAndRegister();
		Kiwi.registerRegistry(CustomizationRegistries.BUILDER_RULE_KEY, BuilderRule.Type.class);
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> {
			if (PlacementSystem.isDebugEnabled(player)) {
				PlacementSystem.removeDebugBlocks(world, pos);
			}
		});
		UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
			return BlockBehaviorRegistry.getInstance().onUseBlock(player, level, hand, hitResult);
		});
		UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
			if (hand == InteractionHand.MAIN_HAND && SitManager.sit(player, hitResult)) {
				return InteractionResult.SUCCESS_SERVER;
			}
			return InteractionResult.PASS;
		});
		CommonLifecycleEvents.TAGS_LOADED.register((registryAccess, client) -> {
			BlockFamilies.reloadTags();
		});
		if (Platform.isPhysicalClient()) {
			CustomizationClient.init();
		}
	}

	public static void initLoader() {
		ResourceManager resourceManager = collectKiwiPacks();
		OneTimeLoader.Context context = new OneTimeLoader.Context();
		Map<String, CustomizationMetadata> metadataMap = CustomizationMetadata.loadMap(resourceManager, context);

		SoundTypes.refreshWithValues(OneTimeLoader.load(
				resourceManager,
				"kiwi/sound_type",
				DeferredSoundType.DIRECT_CODEC.codec(),
				context));

		BlockFundamentals blockFundamentals = BlockFundamentals.reload(resourceManager, context, true);
		clearGlassType = blockFundamentals.glassTypes().get(Identifier.withDefaultNamespace("clear"));
		blockNamespaces.clear();
		blockFundamentals.blocks().keySet().stream().map(Identifier::getNamespace).forEach(blockNamespaces::add);
		lenientBETypeNamespaces.clear();
		lenientBETypeNamespaces.add(Identifier.DEFAULT_NAMESPACE);
		lenientBETypeNamespaces.addAll(blockNamespaces);
		metadataMap.values().forEach(metadata -> lenientBETypeNamespaces.addAll(metadata.lenientBETypeNamespaces()));
		List<Identifier> blockIds = Lists.newArrayList();
		CustomizationMetadata.sortedForEach(
				metadataMap, "block", blockFundamentals.blocks(), (id, definition) -> {
					try {
						ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id);
						Block block = definition.createBlock(key, blockFundamentals.shapes());
						Registry.register(BuiltInRegistries.BLOCK, key, block);
						blockFundamentals.slotProviders().attachSlotsA(block, definition);
						blockFundamentals.placeChoices().attachChoicesA(block, definition);
						blockIds.add(id);
					} catch (Exception e) {
						Kiwi.LOGGER.error("Failed to create block %s".formatted(id), e);
					}
				});

		ToolMaterials.refreshWithValues(OneTimeLoader.load(
				resourceManager,
				"kiwi/tier",
				ToolMaterials.DIRECT_CODEC.codec(),
				context));

		ItemFundamentals itemFundamentals = ItemFundamentals.reload(resourceManager, context, true);
		for (Identifier blockId : blockIds) {
			if (!itemFundamentals.items().containsKey(blockId)) {
				itemFundamentals.addDefaultBlockItem(blockId);
			}
		}
		KItemTemplate none = itemFundamentals.templates().get(Identifier.withDefaultNamespace("none"));
		Preconditions.checkNotNull(none, "Missing 'none' item definition");
		CustomizationMetadata.sortedForEach(
				metadataMap, List.of("item", "block"), itemFundamentals.items(), (id, definition) -> {
					try {
						if (definition.template().template() == none) {
							return;
						}
						ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
						Item item = definition.createItem(key);
						Registry.register(BuiltInRegistries.ITEM, key, item);
					} catch (Exception e) {
						Kiwi.LOGGER.error("Failed to create item %s".formatted(id), e);
					}
				});
		blockFundamentals.slotProviders().attachSlotsB();
		blockFundamentals.placeChoices().attachChoicesB();
		blockFundamentals.slotLinks().finish();
		var tabs = OneTimeLoader.load(resourceManager, "kiwi/creative_tab", KCreativeTab.CODEC, context);
		List<Map.Entry<Identifier, KCreativeTab>> newTabs = tabs.entrySet().stream().sorted(Comparator.comparingInt($ -> $.getValue()
				.order())).filter(entry -> {
			KCreativeTab value = entry.getValue();
			if (value.insert().isPresent()) {
				insertToTab(value);
				return false;
			}
			return true;
		}).toList();
		for (int i = 0; i < newTabs.size(); i++) {
			Map.Entry<Identifier, KCreativeTab> entry = newTabs.get(i);
			Identifier key = entry.getKey();
			KCreativeTab value = entry.getValue();
			CreativeModeTab.Builder tab = AbstractModule.itemCategory(
							key,
							() -> BuiltInRegistries.ITEM.getOptional(value.icon()).orElse(Items.BARRIER).getDefaultInstance())
					.displayItems((params, output) -> {
						output.acceptAll(value.contents()
								.stream()
								.map(BuiltInRegistries.ITEM::getValue)
								.filter(Objects::nonNull)
								.map(Item::getDefaultInstance)
								.toList());
					});
			//TODO fix tab order
//			if (i > 0) {
//				tab.withTabsBefore(CreativeModeTabs.SPAWN_EGGS.location(), newTabs.get(i - 1).getKey());
//			} else {
//				tab.withTabsBefore(CreativeModeTabs.SPAWN_EGGS.location());
//			}
//			if (i < newTabs.size() - 1) {
//				tab.withTabsAfter(newTabs.get(i + 1).getKey());
//			}
			Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, key, tab.build());
		}
		if (Platform.isDataGen()) {
			BlockFamilies.reloadResources(resourceManager, context); // might be useful for data-gen
		}
		if (Platform.isPhysicalClient()) {
			CustomizationClient.afterRegister(
					itemFundamentals.items(),
					blockFundamentals.blocks(),
					new ClientProxy.Context(true));
		}
	}

	private static void insertToTab(KCreativeTab kCreativeTab) {
		if (!Platform.isPhysicalClient()) {
			return;
		}
		CreativeModeTabEvents.modifyOutputEvent(kCreativeTab.insert().orElseThrow()).register(entries -> {
			for (ResourceKey<Item> content : kCreativeTab.contents()) {
				Item item = BuiltInRegistries.ITEM.getValue(content);
				if (item == null) {
					return;
				}
				entries.accept(item);
			}
		});
	}

	public static ResourceManager collectKiwiPacks() {
		//noinspection ResultOfMethodCallIgnored
		CustomizationServiceFinder.PACK_DIRECTORY.toFile().mkdirs();
		var folderRepositorySource = new RequiredFolderRepositorySource(
				CustomizationServiceFinder.PACK_DIRECTORY,
				PackType.CLIENT_RESOURCES,
				PackSource.BUILT_IN,
				new DirectoryValidator(_ -> true));
		PackRepository packRepository = new PackRepository(folderRepositorySource);
//		ResourcePackLoader.loadResourcePacks(packRepository, CustomizationHooks::buildPackFinder);
		packRepository.reload();
//		selected.remove("mod_resources");
//		selected.add(0, "mod_resources");
		return new KiwiPackResourceManager(packRepository.getAvailablePacks().stream().map(Pack::open).toList());
	}

//	private static RepositorySource buildPackFinder(Map<IModFile, ? extends PathPackResources> modResourcePacks) {
//		return packAcceptor -> clientPackFinder(modResourcePacks, packAcceptor);
//	}
//
//	private static void clientPackFinder(Map<IModFile, ? extends PathPackResources> modResourcePacks, Consumer<Pack> packAcceptor) {
//		var hiddenPacks = new ArrayList<PathPackResources>();
//		for (Map.Entry<IModFile, ? extends PathPackResources> e : modResourcePacks.entrySet()) {
//			IModInfo mod = e.getKey().getModInfos().get(0);
//			final String name = "mod:" + mod.getModId();
//			final Pack modPack = Pack.readMetaAndCreate(
//					name,
//					Component.literal(e.getValue().packId()),
//					false,
//					id -> e.getValue(),
//					PackType.CLIENT_RESOURCES,
//					Pack.Position.BOTTOM,
//					PackSource.DEFAULT);
//			if (modPack == null) {
//				// Vanilla only logs an error, instead of propagating, so handle null and warn that something went wrong
//				ModLoader.get().addWarning(new ModLoadingWarning(mod, ModLoadingStage.ERROR, "fml.modloading.brokenresources", e.getKey()));
//				continue;
//			}
//			Kiwi.LOGGER.debug("Generating PackInfo named {} for mod file {}", name, e.getKey().getFilePath());
//			if (mod.getOwningFile().showAsResourcePack()) {
//				packAcceptor.accept(modPack);
//			} else {
//				hiddenPacks.add(e.getValue());
//			}
//		}
//
//		// Create a resource pack merging all mod resources that should be hidden
//		final Pack modResourcesPack = Pack.readMetaAndCreate("mod_resources", Component.literal("Mod Resources"), true,
//				id -> new DelegatingPackResources(id, false, new PackMetadataSection(
//						Component.translatable("fml.resources.modresources", hiddenPacks.size()),
//						SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES)), hiddenPacks),
//				PackType.CLIENT_RESOURCES, Pack.Position.BOTTOM, PackSource.DEFAULT);
//		packAcceptor.accept(modResourcesPack);
//	}

	public static Set<String> getBlockNamespaces() {
		return blockNamespaces;
	}

	public static Set<String> getLenientBETypeNamespaces() {
		return lenientBETypeNamespaces;
	}

	public static boolean isColorlessGlass(BlockState blockState) {
		return blockState.is(ConventionalBlockTags.GLASS_BLOCKS) && !(blockState.getBlock() instanceof StainedGlassBlock);
	}

	public static GlassType clearGlassType() {
		return Objects.requireNonNull(clearGlassType);
	}

	@KiwiModule.LoadingCondition({"block_components", "block_templates", "item_templates", "builder_rules"})
	public static boolean shouldLoad(LoadingContext ctx) {
		return isEnabled();
	}

	public static void frozen() {
		ResourceManager resourceManager = collectKiwiPacks();
		OneTimeLoader.Context context = new OneTimeLoader.Context();
		BlockFamilies.reloadResources(resourceManager, context);
		BuilderRules.reload(resourceManager, context);
	}

	private record BlockStatePairKey(BlockState pState, BlockState pAdjacentBlockState, Direction pDirection) {}
}
