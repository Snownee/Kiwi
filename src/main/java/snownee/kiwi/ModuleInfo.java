package snownee.kiwi;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableObject;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder.ListMultimapBuilder;
import com.google.common.collect.Sets;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.data.loading.DatagenModLoader;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import snownee.kiwi.KiwiModule.Category;
import snownee.kiwi.KiwiModule.RenderLayer;
import snownee.kiwi.block.IKiwiBlock;
import snownee.kiwi.item.ItemCategoryFiller;
import snownee.kiwi.item.ModBlockItem;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.loader.event.ClientInitEvent;
import snownee.kiwi.loader.event.InitEvent;
import snownee.kiwi.loader.event.PostInitEvent;
import snownee.kiwi.loader.event.ServerInitEvent;

public class ModuleInfo {
	public static final class RegistryHolder {
		final Multimap<Object, NamedEntry<?>> registries = ListMultimapBuilder.linkedHashKeys().linkedListValues().build();

		void put(NamedEntry<?> entry) {
			registries.put(entry.registry, entry);
		}

		Collection<NamedEntry<?>> get(Object registry) {
			return registries.get(registry);
		}
	}

	public final AbstractModule module;
	public final ModContext context;
	public GroupSetting groupSetting;
	final RegistryHolder registries = new RegistryHolder();
	final Map<Block, Item.Properties> blockItemBuilders = Maps.newHashMap();
	final Set<Object> noCategories = Sets.newHashSet();
	final Set<Block> noItems = Sets.newHashSet();

	public ModuleInfo(ResourceLocation rl, AbstractModule module, ModContext context) {
		this.module = module;
		this.context = context;
		module.uid = rl;
		if (DatagenModLoader.isRunningDataGen() && context.modContainer instanceof FMLModContainer) {
			((FMLModContainer) context.modContainer).getEventBus().addListener(module::gatherData);
		}
	}

	/**
	 * @since 2.5.2
	 */
	@SuppressWarnings("rawtypes")
	public void register(Object object, ResourceLocation name, Object registry, @Nullable Field field) {
		NamedEntry entry = new NamedEntry(name, object, registry, field);
		registries.put(entry);
		if (field != null) {
			Category group = field.getAnnotation(Category.class);
			if (group != null) {
				entry.groupSetting = GroupSetting.of(group, groupSetting);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public void handleRegister(Object registry) {
		context.setActiveContainer();
		Collection<NamedEntry<?>> entries = registries.get(registry);
		BiConsumer<ModuleInfo, Object> decorator = (BiConsumer<ModuleInfo, Object>) module.decorators.getOrDefault(registry, (a, b) -> {
		});
		if (registry == ForgeRegistries.ITEMS) {
			handleRegister(BuiltInRegistries.CREATIVE_MODE_TAB);
			registries.get(ForgeRegistries.BLOCKS).forEach(e -> {
				if (noItems.contains(e.entry))
					return;
				Item.Properties builder = blockItemBuilders.get(e.entry);
				if (builder == null)
					builder = new Item.Properties();
				BlockItem item;
				if (e.entry instanceof IKiwiBlock) {
					item = ((IKiwiBlock) e.entry).createItem(builder);
				} else {
					item = new ModBlockItem((Block) e.entry, builder);
				}
				if (noCategories.contains(e.entry)) {
					noCategories.add(item);
				}
				NamedEntry itemEntry = new NamedEntry(e.name, item, registry, null);
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
			if (registry instanceof Registry) {
				Registry.register((Registry) registry, e.name, e.entry);
			} else if (registry instanceof IForgeRegistry) {
				((IForgeRegistry) registry).register(e.name, e.entry);
			} else {
				throw new RuntimeException("registry is invalid");
			}
		});
		if (registry == ForgeRegistries.BLOCKS && Platform.isPhysicalClient() && !Platform.isDataGen()) {
			final RenderType solid = RenderType.solid();
			Map<Class<?>, RenderType> cache = Maps.newHashMap();
			entries.stream().forEach(e -> {
				Block block = (Block) e.entry;
				if (e.field != null) {
					RenderLayer layer = e.field.getAnnotation(RenderLayer.class);
					if (layer != null) {
						RenderType type = (RenderType) layer.value().value;
						if (type != solid && type != null) {
							ItemBlockRenderTypes.setRenderLayer(block, type);
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
					ItemBlockRenderTypes.setRenderLayer(block, type);
				}
			});
		}
	}

	public void preInit() {
		context.setActiveContainer();
		module.preInit();
		KiwiModules.ALL_USED_REGISTRIES.addAll(registries.registries.keySet());
	}

	public void init(InitEvent event) {
		context.setActiveContainer();
		module.init(event);
	}

	public void clientInit(ClientInitEvent event) {
		context.setActiveContainer();
		module.clientInit(event);
	}

	public void serverInit(ServerInitEvent event) {
		context.setActiveContainer();
		module.serverInit(event);
	}

	public void postInit(PostInitEvent event) {
		context.setActiveContainer();
		module.postInit(event);
	}

	public <T> List<T> getRegistries(Object registry) {
		return registries.get(registry).stream().map($ -> (T) $.entry).collect(Collectors.toList());
	}

	public <T> Stream<NamedEntry<T>> getRegistryEntries(Object registry) {
		return registries.get(registry).stream().map($ -> (NamedEntry<T>) $);
	}

}
