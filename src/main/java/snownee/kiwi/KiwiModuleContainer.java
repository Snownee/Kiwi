package snownee.kiwi;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder.ListMultimapBuilder;
import com.google.common.collect.Sets;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import snownee.kiwi.block.IKiwiBlock;
import snownee.kiwi.item.ItemCategoryFiller;
import snownee.kiwi.item.ModBlockItem;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.loader.event.InitEvent;
import snownee.kiwi.loader.event.PostInitEvent;
import snownee.kiwi.util.Util;

public final class KiwiModuleContainer {
	public static final class RegistryEntryStore {
		final Multimap<ResourceLocation, KiwiGOHolder<?>> registries = ListMultimapBuilder.linkedHashKeys().linkedListValues().build();

		<T> void put(KiwiGOHolder<T> entry) {
			registries.put(entry.key.registry(), entry);
		}

		@SuppressWarnings("unchecked")
		<T> Collection<KiwiGOHolder<T>> get(ResourceKey<Registry<T>> registry) {
			return (Collection<KiwiGOHolder<T>>) (Object) registries.get(registry.location());
		}
	}

	public final AbstractModule module;
	public final ModContext context;
	public GroupSetting groupSetting;
	final RegistryEntryStore registries = new RegistryEntryStore();
	Map<Block, Item.Properties> blockItemBuilders = Maps.newHashMap();
	Set<Object> noCategories = Sets.newHashSet();
	Set<Block> noItems = Sets.newHashSet();

	public KiwiModuleContainer(ResourceLocation rl, AbstractModule module, ModContext context) {
		this.module = module;
		this.context = context;
		module.uid = rl;
	}

	public <T> void register(T object, ResourceKey<T> key, @Nullable Field field) {
		KiwiGOHolder<T> entry = new KiwiGOHolder<>(object, key, field);
		registries.put(entry);
		if (field != null) {
			KiwiModule.Category group = field.getAnnotation(KiwiModule.Category.class);
			if (group != null) {
				entry.groupSetting = GroupSetting.of(group, groupSetting);
			}
		}
	}

	public void loadGameObjects(RegistryLookup registryLookup) {
		context.setActiveContainer();

		boolean useOwnGroup = groupSetting == null;
		if (useOwnGroup) {
			KiwiModule.Category group = module.getClass().getDeclaredAnnotation(KiwiModule.Category.class);
			if (group != null) {
				if (group.value().length > 0) {
					useOwnGroup = false;
					groupSetting = GroupSetting.of(group, null);
				}
			}
		}

		String modid = module.uid.getNamespace();
		Item.Properties tmpBuilder = null;
		Field tmpBuilderField = null;
		for (Field field : module.getClass().getFields()) {
			if (field.getAnnotation(KiwiModule.Skip.class) != null) {
				continue;
			}

			int mods = field.getModifiers();
			if (!Modifier.isPublic(mods) || !Modifier.isStatic(mods)) {
				continue;
			}

			ResourceLocation id;
			KiwiModule.Name nameAnnotation = field.getAnnotation(KiwiModule.Name.class);
			if (nameAnnotation != null) {
				id = Util.RL(nameAnnotation.value(), modid);
			} else {
				id = Util.RL(field.getName().toLowerCase(Locale.ENGLISH), modid);
			}
			Objects.requireNonNull(id);

			if (field.getType() == module.getClass() && "instance".equals(id.getPath()) && id.getNamespace().equals(modid)) {
				try {
					field.set(null, module);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					Kiwi.LOGGER.error("Kiwi failed to inject module instance to module class: %s".formatted(module.uid), e);
				}
				continue;
			}

			Object o = null;
			try {
				o = field.get(null);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				Kiwi.LOGGER.error("Kiwi failed to catch game object: %s".formatted(field), e);
			}
			if (o == null) {
				continue;
			}
			if (o instanceof Item.Properties properties) {
				tmpBuilder = properties;
				tmpBuilderField = field;
				continue;
			}

			ResourceKey<? extends Registry<?>> registryKey;
			//noinspection rawtypes
			ResourceKey resourceKey;
			if (o instanceof KiwiGO<?> kiwiGO) {
				o = kiwiGO.getOrCreate();
				registryKey = kiwiGO.findRegistry();
				//noinspection unchecked,rawtypes
				resourceKey = ResourceKey.create((ResourceKey) registryKey, id);
				//noinspection unchecked
				kiwiGO.setKey(resourceKey);
			} else {
				registryKey = registryLookup.findRegistry(o);
				if (registryKey == null) {
					tmpBuilder = null;
					tmpBuilderField = null;
					continue;
				}
				//noinspection unchecked,rawtypes
				resourceKey = ResourceKey.create((ResourceKey) registryKey, id);
			}

			if (o instanceof Block) {
				if (field.getAnnotation(KiwiModule.NoItem.class) != null) {
					noItems.add((Block) o);
				}
				checkNoGroup(field, o);
				if (tmpBuilder != null) {
					blockItemBuilders.put((Block) o, tmpBuilder);
					try {
						tmpBuilderField.set(module, null);
					} catch (Exception e) {
						Kiwi.LOGGER.error("Kiwi failed to clean used item builder: %s".formatted(tmpBuilderField), e);
					}
				}
			} else if (o instanceof Item) {
				checkNoGroup(field, o);
			} else if (useOwnGroup && groupSetting == null && o instanceof CreativeModeTab) {
				groupSetting = new GroupSetting(new String[]{id.toString()}, new String[0]);
			}
			//noinspection unchecked
			register(o, resourceKey, field);

			tmpBuilder = null;
			tmpBuilderField = null;
		}
	}

	private void checkNoGroup(Field field, Object o) {
		if (field.getAnnotation(KiwiModule.NoCategory.class) != null) {
			noCategories.add(o);
		}
	}

	public void registerGameObjects(ResourceKey<? extends Registry<?>> registryKey) {
		context.setActiveContainer();
		Collection<KiwiGOHolder<?>> entries = registries.registries.get(registryKey.location());
		BiConsumer<KiwiModuleContainer, KiwiGOHolder<?>> decorator = module.decorators.getOrDefault(registryKey, (a, b) -> {
		});
		if (registryKey == Registries.ITEM) {
			registries.get(Registries.BLOCK).forEach(e -> {
				if (noItems.contains(e.value))
					return;
				Item.Properties builder = blockItemBuilders.get(e.value);
				if (builder == null)
					builder = new Item.Properties();
				BlockItem item;
				if (e.value instanceof IKiwiBlock kiwiBlock) {
					item = kiwiBlock.createItem(builder);
				} else {
					item = new ModBlockItem(e.value, builder);
				}
				if (noCategories.contains(e.value)) {
					noCategories.add(item);
				}
				KiwiGOHolder<Item> itemEntry = new KiwiGOHolder<>(item, ResourceKey.create(Registries.ITEM, e.key.location()), null);
				itemEntry.groupSetting = e.groupSetting;
				entries.add(itemEntry);
			});
			Set<GroupSetting> groupSettings = Sets.newLinkedHashSet();
			MutableObject<GroupSetting> prevSetting = new MutableObject<>();
			if (groupSetting != null) {
				prevSetting.setValue(groupSetting);
				groupSettings.add(groupSetting);
			}
			entries.forEach(e -> {
				Item item = (Item) e.value;
				if (noCategories.contains(item)) {
					prevSetting.setValue(groupSetting);
					return;
				}
				ItemCategoryFiller filler;
				if (item instanceof ItemCategoryFiller) {
					filler = (ItemCategoryFiller) item;
				} else {
					filler = (tab, flags, hasPermissions, items) -> items.add(new ItemStack(item));
				}
				if (e.groupSetting != null) {
					e.groupSetting.apply(filler);
					groupSettings.add(e.groupSetting);
					prevSetting.setValue(e.groupSetting);
				} else if (prevSetting.getValue() != null) {
					prevSetting.getValue().apply(filler);
				}
			});
			groupSettings.forEach(GroupSetting::postApply);
		}
		entries.forEach(e -> {
			decorator.accept(this, e);
			e.register();
		});
		if (registryKey == Registries.ITEM) {
			blockItemBuilders = null;
			noCategories = null;
			noItems = null;
		} else if (registryKey == Registries.BLOCK && Platform.isPhysicalClient() && !Platform.isDataGen()) {
			final RenderType solid = RenderType.solid();
			Map<Class<?>, RenderType> cache = Maps.newHashMap();
			entries.forEach(e -> {
				Block block = (Block) e.value;
				if (e.field != null) {
					KiwiModule.RenderLayer layer = e.field.getAnnotation(KiwiModule.RenderLayer.class);
					if (layer != null) {
						RenderType type = (RenderType) layer.value().value;
						if (type != solid && type != null) {
							BlockRenderLayerMap.INSTANCE.putBlock(block, type);
							return;
						}
					}
				}
				Class<?> klass = block.getClass();
				RenderType type = cache.computeIfAbsent(klass, k -> {
					KiwiModule.RenderLayer layer;
					while (k != Block.class) {
						layer = k.getDeclaredAnnotation(KiwiModule.RenderLayer.class);
						if (layer != null) {
							return (RenderType) layer.value().value;
						}
						k = k.getSuperclass();
					}
					return solid;
				});
				if (type != solid && type != null) {
					BlockRenderLayerMap.INSTANCE.putBlock(block, type);
				}
			});
		}
	}

	public void addRegistries() {
		context.setActiveContainer();
		module.addRegistries();
	}

	public void addEntries() {
		context.setActiveContainer();
		module.addEntries();
		registries.registries.keySet().stream()
				.map(ResourceKey::createRegistryKey)
				.forEach(KiwiModules.ALL_USED_REGISTRIES::add);
		KiwiModules.ALL_USED_REGISTRIES.forEach(this::registerGameObjects);
	}

	public void init(InitEvent event) {
		context.setActiveContainer();
		module.init(event);
	}

	public void postInit(PostInitEvent event) {
		context.setActiveContainer();
		module.postInit(event);
	}

	public <T> List<T> getRegistries(ResourceKey<Registry<T>> registry) {
		return getRegistryEntries(registry).map($ -> $.value).toList();
	}

	public <T> Stream<KiwiGOHolder<T>> getRegistryEntries(ResourceKey<Registry<T>> registry) {
		return registries.get(registry).stream();
	}

}
