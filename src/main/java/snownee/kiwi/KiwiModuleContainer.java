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
import snownee.kiwi.loader.ClientPlatform;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.loader.event.InitEvent;
import snownee.kiwi.loader.event.PostInitEvent;
import snownee.kiwi.util.KUtil;

public final class KiwiModuleContainer {
	public static final class RegistryEntryStore {
		final Multimap<ResourceLocation, KiwiGO<?>> registries = ListMultimapBuilder.linkedHashKeys().linkedListValues().build();

		<T> void put(KiwiGO<T> entry) {
			registries.put(entry.resourceKey().registry(), entry);
		}

		@SuppressWarnings("unchecked")
		<T> Collection<KiwiGO<T>> get(ResourceKey<Registry<T>> registry) {
			return (Collection<KiwiGO<T>>) (Object) registries.get(registry.location());
		}
	}

	public final AbstractModule module;
	public final ModContext context;
	public GroupSetting groupSetting;
	final RegistryEntryStore registries = new RegistryEntryStore();
	Map<Block, Item.Properties> blockItemBuilders = Maps.newHashMap();
	Set<Object> noCategories = Sets.newHashSet();
	Set<Block> noItems = Sets.newHashSet();

	public KiwiModuleContainer(ResourceLocation id, AbstractModule module, ModContext context) {
		this.module = module;
		this.context = context;
		module.uid = id;
	}

	public <T> void register(KiwiGO<T> object, @Nullable Field field) {
		registries.put(object);
		if (field != null) {
			object.field = field;
			KiwiModule.Category group = field.getAnnotation(KiwiModule.Category.class);
			if (group != null) {
				object.groupSetting = GroupSetting.of(group, groupSetting);
			}
		}
	}

	public void loadGameObjects() {
		context.setActiveContainer();

		final boolean useOwnGroup;
		if (groupSetting == null) {
			KiwiModule.Category group = module.getClass().getDeclaredAnnotation(KiwiModule.Category.class);
			if (group != null && group.value().length > 0) {
				useOwnGroup = false;
				groupSetting = GroupSetting.of(group, null);
			} else {
				useOwnGroup = true;
			}
		} else {
			useOwnGroup = false;
		}

		String modId = module.uid.getNamespace();
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
				id = KUtil.RL(nameAnnotation.value(), modId);
			} else {
				id = KUtil.RL(field.getName().toLowerCase(Locale.ENGLISH), modId);
			}
			Objects.requireNonNull(id);

			if (field.getType() == module.getClass() && "instance".equals(id.getPath()) && id.getNamespace().equals(modId)) {
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

			if (!(o instanceof KiwiGO<?> kiwiGO)) {
				continue;
			}
			o = kiwiGO.getOrCreate();
			ResourceKey<? extends Registry<?>> registryKey = kiwiGO.findRegistry();
			//noinspection unchecked,rawtypes
			ResourceKey resourceKey = ResourceKey.create((ResourceKey) registryKey, id);
			//noinspection unchecked
			kiwiGO.setKey(resourceKey);

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
			} else if (o instanceof CreativeModeTab && useOwnGroup && groupSetting == null) {
				groupSetting = new GroupSetting(new String[]{id.toString()}, null);
			}
			register(kiwiGO, field);
			if (Registries.MOB_EFFECT == registryKey) {
				BiConsumer<KiwiModuleContainer, KiwiGO<?>> decorator = module.decorators.getOrDefault(
						registryKey, (a, b) -> {
						});
				decorator.accept(this, kiwiGO);
				kiwiGO.register();
			}

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
		if (Registries.MOB_EFFECT == registryKey) {
			// Potion's constructor wants a Holder<MobEffect>, early loading it
			return;
		}
		context.setActiveContainer();
		Collection<KiwiGO<?>> entries = registries.registries.get(registryKey.location());
		BiConsumer<KiwiModuleContainer, KiwiGO<?>> decorator = module.decorators.getOrDefault(
				registryKey, (a, b) -> {
				});
		if (Registries.ITEM == registryKey) {
			registries.get(Registries.BLOCK).forEach(e -> {
				if (noItems.contains(e.get())) {
					return;
				}
				Item.Properties builder = blockItemBuilders.get(e.get());
				if (builder == null) {
					builder = new Item.Properties();
				}
				BlockItem item;
				if (e.get() instanceof IKiwiBlock kiwiBlock) {
					item = kiwiBlock.createItem(builder);
				} else {
					item = new ModBlockItem(e.get(), builder);
				}
				if (noCategories.contains(e.get())) {
					noCategories.add(item);
				}
				KiwiGO<Item> itemEntry = new KiwiGO.Direct<>(item);
				itemEntry.setKey(ResourceKey.create(Registries.ITEM, e.key()));
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
				Item item = (Item) e.get();
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
		if (Registries.ITEM == registryKey) {
			blockItemBuilders = null;
			noCategories = null;
			noItems = null;
		} else if (Registries.BLOCK == registryKey && Platform.isPhysicalClient() && !Platform.isDataGen()) {
			final RenderType solid = RenderType.solid();
			Map<Class<?>, RenderType> cache = Maps.newHashMap();
			entries.forEach(e -> {
				Block block = (Block) e.get();
				if (e.field != null) {
					KiwiModule.RenderLayer layer = e.field.getAnnotation(KiwiModule.RenderLayer.class);
					if (layer != null) {
						RenderType type = (RenderType) layer.value().value;
						if (type != solid && type != null) {
							ClientPlatform.setRenderType(block, type);
							return;
						}
					}
				}
				Class<?> klass = block.getClass();
				RenderType type = cache.computeIfAbsent(
						klass, k -> {
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
					ClientPlatform.setRenderType(block, type);
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
		return getRegistryEntries(registry).map(KiwiGO::get).toList();
	}

	public <T> Stream<KiwiGO<T>> getRegistryEntries(ResourceKey<Registry<T>> registry) {
		return registries.get(registry).stream();
	}

}
