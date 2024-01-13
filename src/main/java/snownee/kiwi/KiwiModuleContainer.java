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

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import snownee.kiwi.KiwiModule.Category;
import snownee.kiwi.KiwiModule.RenderLayer;
import snownee.kiwi.block.IKiwiBlock;
import snownee.kiwi.item.ItemCategoryFiller;
import snownee.kiwi.item.ModBlockItem;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.loader.event.InitEvent;
import snownee.kiwi.loader.event.PostInitEvent;
import snownee.kiwi.util.Util;

public final class KiwiModuleContainer {
	public static final class RegistryHolder {
		final Multimap<Registry<?>, NamedEntry<?>> registries = ListMultimapBuilder.linkedHashKeys().linkedListValues().build();

		<T> void put(NamedEntry<T> entry) {
			registries.put(entry.registry, entry);
		}

		<T> Collection<NamedEntry<T>> get(Registry<T> registry) {
			return (Collection<NamedEntry<T>>) (Object) registries.get(registry);
		}
	}

	public final AbstractModule module;
	public final ModContext context;
	public GroupSetting groupSetting;
	final RegistryHolder registries = new RegistryHolder();
	final Map<Block, Item.Properties> blockItemBuilders = Maps.newHashMap();
	final Set<Object> noCategories = Sets.newHashSet();
	final Set<Block> noItems = Sets.newHashSet();

	public KiwiModuleContainer(ResourceLocation rl, AbstractModule module, ModContext context) {
		this.module = module;
		this.context = context;
		module.uid = rl;
	}

	@SuppressWarnings("rawtypes")
	public void register(Object object, ResourceLocation name, Registry<?> registry, @Nullable Field field) {
		NamedEntry entry = new NamedEntry(name, object, registry, field);
		registries.put(entry);
		if (field != null) {
			Category group = field.getAnnotation(Category.class);
			if (group != null) {
				entry.groupSetting = GroupSetting.of(group, groupSetting);
			}
		}
	}

	public void loadGameObjects(RegistryLookup registryLookup, Object2IntMap<ResourceKey<?>> counter) {
		context.setActiveContainer();

		boolean useOwnGroup = groupSetting == null;
		if (useOwnGroup) {
			Category group = module.getClass().getDeclaredAnnotation(Category.class);
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

			ResourceLocation regName;
			KiwiModule.Name nameAnnotation = field.getAnnotation(KiwiModule.Name.class);
			if (nameAnnotation != null) {
				regName = Util.RL(nameAnnotation.value(), modid);
			} else {
				regName = Util.RL(field.getName().toLowerCase(Locale.ENGLISH), modid);
			}
			Objects.requireNonNull(regName);

			if (field.getType() == module.getClass() && "instance".equals(regName.getPath()) && regName.getNamespace().equals(modid)) {
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
			if (o instanceof Item.Properties) {
				tmpBuilder = (Item.Properties) o;
				tmpBuilderField = field;
				continue;
			}

			Registry<?> registry;
			if (o instanceof KiwiGO<?> kiwiGO) {
				kiwiGO.setKey(regName);
				o = kiwiGO.getOrCreate();
				registry = (Registry<?>) kiwiGO.registry();
			} else {
				registry = registryLookup.findRegistry(o);
			}

			if (registry != null) {
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
				} else if (useOwnGroup && groupSetting == null && o instanceof CreativeModeTab tab) {
					//registerTab(id, tab);
					groupSetting = new GroupSetting(new String[]{regName.toString()}, new String[0]);
				}
				ResourceKey<?> superType = registry.key();
				int i = counter.getOrDefault(superType, 0);
				counter.put(superType, i + 1);
				register(o, regName, registry, field);
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

	@SuppressWarnings("rawtypes")
	public <T> void registerGameObjects(Object registry) {
		context.setActiveContainer();
		Collection<NamedEntry<T>> entries = registries.get((Registry<T>) registry);
		BiConsumer<KiwiModuleContainer, T> decorator = (BiConsumer<KiwiModuleContainer, T>) module.decorators.getOrDefault(registry, (a, b) -> {
		});
		if (registry == BuiltInRegistries.ITEM) {
			registries.get(BuiltInRegistries.BLOCK).forEach(e -> {
				if (noItems.contains(e.entry))
					return;
				Item.Properties builder = blockItemBuilders.get(e.entry);
				if (builder == null)
					builder = new Item.Properties();
				BlockItem item;
				if (e.entry instanceof IKiwiBlock) {
					item = ((IKiwiBlock) e.entry).createItem(builder);
				} else {
					item = new ModBlockItem(e.entry, builder);
				}
				if (noCategories.contains(e.entry)) {
					noCategories.add(item);
				}
				NamedEntry itemEntry = new NamedEntry(e.name, item, (Registry) registry, null);
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
				Item item = (Item) e.entry;
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
			decorator.accept(this, e.entry);
			Registry.register(e.registry, e.name, e.entry);
		});
		if (registry == BuiltInRegistries.BLOCK && Platform.isPhysicalClient() && !Platform.isDataGen()) {
			final RenderType solid = RenderType.solid();
			Map<Class<?>, RenderType> cache = Maps.newHashMap();
			entries.stream().forEach(e -> {
				Block block = (Block) e.entry;
				if (e.field != null) {
					RenderLayer layer = e.field.getAnnotation(RenderLayer.class);
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
					RenderLayer layer;
					while (k != Block.class) {
						layer = k.getDeclaredAnnotation(RenderLayer.class);
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

	public void preInit() {
		context.setActiveContainer();
		module.preInit();
		KiwiModules.ALL_USED_REGISTRIES.addAll(registries.registries.keySet());
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

	public <T> List<T> getRegistries(Registry<T> registry) {
		return registries.get(registry).stream().map($ -> $.entry).toList();
	}

	public <T> Stream<NamedEntry<T>> getRegistryEntries(Registry<T> registry) {
		return registries.get(registry).stream();
	}

}
