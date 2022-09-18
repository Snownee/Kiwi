package snownee.kiwi;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder.ListMultimapBuilder;
import com.google.common.collect.Sets;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import snownee.kiwi.KiwiModule.Category;
import snownee.kiwi.KiwiModule.RenderLayer;
import snownee.kiwi.block.IKiwiBlock;
import snownee.kiwi.item.ModBlockItem;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.loader.event.ClientInitEvent;
import snownee.kiwi.loader.event.InitEvent;
import snownee.kiwi.loader.event.PostInitEvent;
import snownee.kiwi.loader.event.ServerInitEvent;
import snownee.kiwi.mixin.ItemAccess;

public class ModuleInfo {
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
	public CreativeModeTab category;
	final RegistryHolder registries = new RegistryHolder();
	final Map<Block, Item.Properties> blockItemBuilders = Maps.newHashMap();
	final Set<Object> noCategories = Sets.newHashSet();
	final Set<Block> noItems = Sets.newHashSet();

	public ModuleInfo(ResourceLocation rl, AbstractModule module, ModContext context) {
		this.module = module;
		this.context = context;
		module.uid = rl;
		//		if (FabricDataGenHelper.ENABLED && context.modContainer instanceof FMLModContainer) {
		//			((FMLModContainer) context.modContainer).getEventBus().addListener(module::gatherData);
		//		}
	}

	/**
	 * @since 2.5.2
	 */
	@SuppressWarnings("rawtypes")
	public void register(Object entry, ResourceLocation name, Registry<?> registry, @Nullable Field field) {
		registries.put(new NamedEntry(name, entry, registry, field));
	}

	@SuppressWarnings("rawtypes")
	public <T> void handleRegister(Registry<T> registry) {
		context.setActiveContainer();
		Collection<NamedEntry<T>> entries = registries.get(registry);
		BiConsumer<ModuleInfo, T> decorator = (BiConsumer<ModuleInfo, T>) module.decorators.getOrDefault(registry, (a, b) -> {
		});
		if (registry == Registry.ITEM) {
			registries.get(Registry.BLOCK).forEach(e -> {
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
				} else if (e.field != null) {
					Category group = e.field.getAnnotation(Category.class);
					if (group != null && !group.value().isEmpty()) {
						CreativeModeTab category = Kiwi.getGroup(group.value());
						if (category != null) {
							((ItemAccess) item).setCategory(category);
						} else {
							((ItemAccess) item).setCategory(this.category);
						}
					}
				}
				entries.add(new NamedEntry(e.name, item, registry, null));
			});
			entries.forEach(e -> {
				if (e.field != null) {
					Category group = e.field.getAnnotation(Category.class);
					if (group != null && !group.value().isEmpty()) {
						CreativeModeTab category = Kiwi.getGroup(group.value());
						if (category != null) {
							((ItemAccess) e.entry).setCategory(category);
						} else {
							((ItemAccess) e.entry).setCategory(this.category);
						}
					}
				}
			});
		}
		entries.forEach(e -> {
			decorator.accept(this, e.entry);
			Registry.register(e.registry, e.name, e.entry);
		});
		if (registry == Registry.BLOCK && Platform.isPhysicalClient() && !Platform.isDataGen()) {
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
					RenderLayer layer = null;
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
		KiwiModules.ALL_USED_REGISTRIES.addAll(registries.registries.keySet());
		context.setActiveContainer();
		module.preInit();
		registries.registries.keySet().forEach(this::handleRegister);
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

	public <T> List<T> getRegistries(Registry<T> registry) {
		return registries.get(registry).stream().map($ -> $.entry).toList();
	}

}
