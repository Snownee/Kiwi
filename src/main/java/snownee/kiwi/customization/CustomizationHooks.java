package snownee.kiwi.customization;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.loading.ClientModLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import net.neoforged.neoforge.resource.ResourcePackLoader;
import net.neoforged.neoforgespi.locating.IModFile;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.Kiwi;
import snownee.kiwi.customization.block.BlockFundamentals;
import snownee.kiwi.customization.block.GlassType;
import snownee.kiwi.customization.block.KBlockSettings;
import snownee.kiwi.customization.block.behavior.BlockBehaviorRegistry;
import snownee.kiwi.customization.block.behavior.SitManager;
import snownee.kiwi.customization.block.component.KBlockComponent;
import snownee.kiwi.customization.block.family.BlockFamilies;
import snownee.kiwi.customization.block.loader.KBlockTemplate;
import snownee.kiwi.customization.builder.BuilderRules;
import snownee.kiwi.customization.item.ItemFundamentals;
import snownee.kiwi.customization.item.loader.KCreativeTab;
import snownee.kiwi.customization.item.loader.KItemTemplate;
import snownee.kiwi.customization.placement.PlacementSystem;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.util.ClientProxy;
import snownee.kiwi.util.resource.OneTimeLoader;
import snownee.kiwi.util.resource.RequiredFolderRepositorySource;

@Mod(Kiwi.ID)
public final class CustomizationHooks {
	private static final Set<String> blockNamespaces = Sets.newLinkedHashSet();
	private static boolean enabled = true;
	public static boolean kswitch = Platform.isModLoaded("kswitch") || !Platform.isProduction();
	@Nullable
	private static GlassType clearGlassType;

	private CustomizationHooks() {
	}

	// a custom implementation of the Block.shouldRenderFace
	private static final int CACHE_SIZE = 512;
	private static final ThreadLocal<Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>> OCCLUSION_CACHE = ThreadLocal.withInitial(() -> {
		Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> object2bytelinkedopenhashmap = new Object2ByteLinkedOpenHashMap<>(
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
		Block.BlockStatePairKey key = new Block.BlockStatePairKey(pState, pAdjacentBlockState, pDirection);
		Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> map = OCCLUSION_CACHE.get();
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

	public CustomizationHooks(IEventBus modEventBus) {
		enabled = CustomizationServiceFinder.shouldEnable(ModList.get().getMods());
		if (!isEnabled()) {
			return;
		}
		Kiwi.LOGGER.info("Kiwi Customization is enabled");
		var forgeEventBus = NeoForge.EVENT_BUS;
		modEventBus.addListener(EventPriority.LOWEST, (RegisterEvent event) -> {
			if (!Registries.BLOCK.equals(event.getRegistryKey())) {
				return;
			}
			initLoader(modEventBus);
		});
		modEventBus.addListener((NewRegistryEvent event) -> {
			CustomizationRegistries.BLOCK_COMPONENT = event.create(new RegistryBuilder<>(CustomizationRegistries.BLOCK_COMPONENT_KEY));
			Kiwi.registerRegistry(CustomizationRegistries.BLOCK_COMPONENT_KEY, KBlockComponent.Type.class);
			CustomizationRegistries.BLOCK_TEMPLATE = event.create(new RegistryBuilder<>(CustomizationRegistries.BLOCK_TEMPLATE_KEY));
			Kiwi.registerRegistry(CustomizationRegistries.BLOCK_TEMPLATE_KEY, KBlockTemplate.Type.class);
			CustomizationRegistries.ITEM_TEMPLATE = event.create(new RegistryBuilder<>(CustomizationRegistries.ITEM_TEMPLATE_KEY));
			Kiwi.registerRegistry(CustomizationRegistries.ITEM_TEMPLATE_KEY, KItemTemplate.Type.class);
		});
		modEventBus.addListener((AddPackFindersEvent event) -> {
			event.addRepositorySource(new RequiredFolderRepositorySource(
					CustomizationServiceFinder.PACK_DIRECTORY,
					event.getPackType(),
					PackSource.BUILT_IN,
					new DirectoryValidator($ -> true)
					// For Snownee: this validates content path. For now, it accepts everything, but you can do something with it later.
			));
		});
		forgeEventBus.addListener((BlockEvent.BreakEvent event) -> {
			if (PlacementSystem.isDebugEnabled(event.getPlayer())) {
				PlacementSystem.removeDebugBlocks(event.getPlayer().level(), event.getPos());
			}
		});
		forgeEventBus.addListener((PlayerInteractEvent.RightClickBlock event) -> {
			InteractionResult result = BlockBehaviorRegistry.getInstance().onUseBlock(
					event.getEntity(),
					event.getLevel(),
					event.getHand(),
					event.getHitVec());
			if (result.consumesAction()) {
				event.setCanceled(true);
				event.setCancellationResult(result);
			}
		});
		forgeEventBus.addListener((PlayerInteractEvent.RightClickBlock event) -> {
			if (event.getHand() == InteractionHand.MAIN_HAND && SitManager.sit(event.getEntity(), event.getHitVec())) {
				event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
				event.setCanceled(true);
			}
		});
		forgeEventBus.addListener((TagsUpdatedEvent event) -> {
			BlockFamilies.reloadTags();
		});

		if (Platform.isPhysicalClient()) {
			CustomizationClient.init(modEventBus);
		}
	}

	public static void initLoader(IEventBus modEventBus) {
		ResourceManager resourceManager = collectKiwiPacks();
		OneTimeLoader.Context context = new OneTimeLoader.Context();
		Map<String, CustomizationMetadata> metadataMap = CustomizationMetadata.loadMap(resourceManager, context);
		BlockFundamentals blockFundamentals = BlockFundamentals.reload(resourceManager, context, true);
		clearGlassType = blockFundamentals.glassTypes().get(ResourceLocation.withDefaultNamespace("clear"));
		Preconditions.checkNotNull(clearGlassType, "Missing 'clear' glass type");
		blockNamespaces.clear();
		blockFundamentals.blocks().keySet().stream().map(ResourceLocation::getNamespace).forEach(blockNamespaces::add);
		List<ResourceLocation> blockIds = Lists.newArrayList();
		CustomizationMetadata.sortedForEach(metadataMap, "block", blockFundamentals.blocks(), (id, definition) -> {
			try {
				Block block = definition.createBlock(id, blockFundamentals.shapes());
				if (block == null) {
					return;
				}
				Registry.register(BuiltInRegistries.BLOCK, id, block);
				blockFundamentals.slotProviders().attachSlotsA(block, definition);
				blockFundamentals.placeChoices().attachChoicesA(block, definition);
				blockIds.add(id);
			} catch (Exception e) {
				Kiwi.LOGGER.error("Failed to create block %s".formatted(id), e);
			}
		});
		ItemFundamentals itemFundamentals = ItemFundamentals.reload(resourceManager, context, true);
		for (ResourceLocation blockId : blockIds) {
			if (!itemFundamentals.items().containsKey(blockId)) {
				itemFundamentals.addDefaultBlockItem(blockId);
			}
		}
		KItemTemplate none = itemFundamentals.templates().get(ResourceLocation.withDefaultNamespace("none"));
		Preconditions.checkNotNull(none, "Missing 'none' item definition");
		CustomizationMetadata.sortedForEach(metadataMap, "item", itemFundamentals.items(), (id, definition) -> {
			try {
				if (definition.template().template() == none) {
					return;
				}
				Item item = definition.createItem(id);
				if (item == null) {
					return;
				}
				Registry.register(BuiltInRegistries.ITEM, id, item);
			} catch (Exception e) {
				Kiwi.LOGGER.error("Failed to create item %s".formatted(id), e);
			}
		});
		blockFundamentals.slotProviders().attachSlotsB();
		blockFundamentals.placeChoices().attachChoicesB();
		blockFundamentals.slotLinks().finish();
		if (Platform.isPhysicalClient()) {
			CustomizationClient.afterRegister(
					itemFundamentals.items(),
					blockFundamentals.blocks(),
					new ClientProxy.Context(ClientModLoader.isLoading(), modEventBus));
		}
		var tabs = OneTimeLoader.load(resourceManager, "kiwi/creative_tab", KCreativeTab.CODEC, context);
		List<Map.Entry<ResourceLocation, KCreativeTab>> newTabs = tabs.entrySet().stream().sorted(Comparator.comparingInt($ -> $.getValue()
				.order())).filter(entry -> {
			KCreativeTab value = entry.getValue();
			if (value.insert().isPresent()) {
				insertToTab(modEventBus, value);
				return false;
			}
			return true;
		}).toList();
		for (int i = 0; i < newTabs.size(); i++) {
			Map.Entry<ResourceLocation, KCreativeTab> entry = newTabs.get(i);
			ResourceLocation key = entry.getKey();
			KCreativeTab value = entry.getValue();
			CreativeModeTab.Builder tab = AbstractModule.itemCategory(
							key,
							() -> BuiltInRegistries.ITEM.getOptional(value.icon()).orElse(Items.BARRIER).getDefaultInstance())
					.displayItems((params, output) -> {
						output.acceptAll(value.contents()
								.stream()
								.map(BuiltInRegistries.ITEM::get)
								.filter(Objects::nonNull)
								.map(Item::getDefaultInstance)
								.toList());
					});
			if (i > 0) {
				tab.withTabsBefore(newTabs.get(i - 1).getKey());
			}
			if (i < newTabs.size() - 1) {
				tab.withTabsAfter(newTabs.get(i + 1).getKey());
			}
			Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, key, tab.build());
		}
		BlockFamilies.reloadResources(resourceManager, context); // might be useful for data-gen
		if (!Platform.isDataGen()) {
			BuilderRules.reload(resourceManager, context);
		}
	}

	private static void insertToTab(IEventBus modEventBus, KCreativeTab kCreativeTab) {
		if (!Platform.isPhysicalClient()) {
			return;
		}
		modEventBus.addListener((BuildCreativeModeTabContentsEvent event) -> {
			if (event.getTabKey() != kCreativeTab.insert().orElseThrow()) {
				return;
			}
			for (ResourceKey<Item> content : kCreativeTab.contents()) {
				Item item = BuiltInRegistries.ITEM.get(content);
				if (item == null) {
					return;
				}
				event.accept(item);
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
				new DirectoryValidator($ -> true));
		PackRepository packRepository = new PackRepository(folderRepositorySource);
		Map<IModFile, Pack.ResourcesSupplier> kiwiPacks = new HashMap<>();
		for (var modFileInfo : ModList.get().getModFiles()) {
			for (var modInfo : modFileInfo.getMods()) {
				if (modInfo.getModProperties().containsKey("kiwiCustomization")) {
					var modResourcePack = ResourcePackLoader.createPackForMod(modFileInfo);
					kiwiPacks.put(modFileInfo.getFile(), modResourcePack);
					break;
				}
			}
		}
		packRepository.addPackFinder(ResourcePackLoader.buildPackFinder(kiwiPacks, PackType.SERVER_DATA));
		packRepository.reload();
		List<String> selected = Lists.newArrayList(packRepository.getAvailableIds());
		//selected.remove("mod_resources"); // As in 1.20-fabric
		//selected.add(0, "mod_resources"); // As in 1.20-fabric
		packRepository.setSelected(selected);
		return new KiwiPackResourceManager(packRepository.openAllSelected());
	}

	// As in 1.20-fabric
/*	private static RepositorySource buildPackFinder(Map<IModFile, ? extends PathPackResources> modResourcePacks) {
		return packAcceptor -> clientPackFinder(modResourcePacks, packAcceptor);
	}

	private static void clientPackFinder(Map<IModFile, ? extends PathPackResources> modResourcePacks, Consumer<Pack> packAcceptor) {
		var hiddenPacks = new ArrayList<PathPackResources>();
		for (Map.Entry<IModFile, ? extends PathPackResources> e : modResourcePacks.entrySet()) {
			IModInfo mod = e.getKey().getModInfos().get(0);
			final String name = "mod:" + mod.getModId();
			final Pack modPack = Pack.readMetaAndCreate(
					new PackLocationInfo(name, Component.literal(e.getValue().packId()), PackSource.DEFAULT, Optional.empty()),
					new Pack.ResourcesSupplier() {
						@Override
						public PackResources openPrimary(PackLocationInfo p_326301_) {
							return e.getValue();
						}

						@Override
						public PackResources openFull(PackLocationInfo p_326241_, Pack.Metadata p_325959_) {
							return e.getValue();
						}
					},
					PackType.CLIENT_RESOURCES,
					new PackSelectionConfig(false, Pack.Position.BOTTOM, false));
			if (modPack == null) {
				// Vanilla only logs an error, instead of propagating, so handle null and warn that something went wrong
				LoadingModList.get().getModLoadingIssues().add(new ModLoadingIssue(ModLoadingIssue.Severity.ERROR, "fml.modloading.brokenresources", List.of(e.getKey().getFileName())));
				continue;
			}
			Kiwi.LOGGER.debug("Generating PackInfo named {} for mod file {}", name, e.getKey().getFilePath());
			if (mod.getOwningFile().showAsResourcePack()) {
				packAcceptor.accept(modPack);
			} else {
				hiddenPacks.add(e.getValue());
			}
		}

		// Create a resource pack merging all mod resources that should be hidden
		final Pack modResourcesPack = Pack.readMetaAndCreate("mod_resources", Component.literal("Mod Resources"), true,
				id -> new DelegatingPackResources(id, false, new PackMetadataSection(
						Component.translatable("fml.resources.modresources", hiddenPacks.size()),
						SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES)), hiddenPacks),
				PackType.CLIENT_RESOURCES, Pack.Position.BOTTOM, PackSource.DEFAULT);
		packAcceptor.accept(modResourcesPack);
	}*/

	public static Set<String> getBlockNamespaces() {
		return blockNamespaces;
	}

	public static boolean isColorlessGlass(BlockState blockState) {
		return blockState.is(Tags.Blocks.GLASS_BLOCKS_COLORLESS);
	}

	public static GlassType clearGlassType() {
		return clearGlassType;
	}
}
