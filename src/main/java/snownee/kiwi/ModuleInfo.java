package snownee.kiwi;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fmlserverevents.FMLServerStartingEvent;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;
import snownee.kiwi.KiwiModule.Category;
import snownee.kiwi.item.ModBlockItem;

public class ModuleInfo {
	public static final class RegistryHolder {
		final Multimap<Class<?>, NamedEntry<?>> registries = LinkedListMultimap.create();

		<T extends IForgeRegistryEntry<T>> void put(NamedEntry<T> entry) {
			registries.put(entry.entry.getRegistryType(), entry);
		}

		<T extends IForgeRegistryEntry<T>> Collection<NamedEntry<T>> get(Class<T> clazz) {
			return registries.get(clazz).stream().map(e -> (NamedEntry<T>) e).collect(Collectors.toList());
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
	}

	/**
	 * @since 2.5.2
	 */
	@SuppressWarnings("rawtypes")
	public void register(IForgeRegistryEntry<?> entry, ResourceLocation name, @Nullable Field field) {
		registries.put(new NamedEntry(name, entry, field));
	}

	@SuppressWarnings("rawtypes")
	public <T extends IForgeRegistryEntry<T>> void handleRegister(RegistryEvent.Register<T> event) {
		context.setActiveContainer();
		Class<T> clazz = event.getRegistry().getRegistrySuperType();
		Collection<NamedEntry<T>> entries = registries.get(clazz);
		BiConsumer<ModuleInfo, T> decorator = (BiConsumer<ModuleInfo, T>) module.decorators.getOrDefault(clazz, (a, b) -> {
		});
		if (clazz == Item.class) {
			registries.get(Block.class).forEach(e -> {
				if (noItems.contains(e.entry))
					return;
				Item.Properties builder = blockItemBuilders.get(e.entry);
				if (builder == null)
					builder = new Item.Properties();
				ModBlockItem item = new ModBlockItem(e.entry, builder);
				if (noCategories.contains(e.entry)) {
					noCategories.add(item);
				} else if (e.field != null) {
					Category group = e.field.getAnnotation(Category.class);
					if (group != null && !group.value().isEmpty()) {
						CreativeModeTab category = Kiwi.getGroup(group.value());
						if (category != null) {
							item.category = category;
						} else {
							item.category = this.category;
						}
					}
				}
				entries.add(new NamedEntry(e.name, item));
			});
		}
		entries.forEach(e -> {
			decorator.accept(this, e.entry.setRegistryName(e.name));
			event.getRegistry().register(e.entry);
		});
		if (clazz == Block.class && FMLEnvironment.dist.isClient()) {
			final RenderType solid = RenderType.solid();
			Map<Class<?>, RenderType> cache = Maps.newHashMap();
			entries.stream().forEach(e -> {
				Block block = (Block) e.entry;
				if (e.field != null) {
					RenderLayer layer = e.field.getAnnotation(RenderLayer.class);
					if (layer != null) {
						RenderType type = layer.value().get();
						if (type != solid && type != null) {
							ItemBlockRenderTypes.setRenderLayer(block, type);
							return;
						}
					}
				}
				Class<?> klass = block.getClass();
				RenderType type = cache.computeIfAbsent(klass, k -> {
					RenderLayer layer = null;
					while (k != Block.class) {
						layer = k.getAnnotation(RenderLayer.class);
						if (layer != null) {
							return layer.value().get();
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
	}

	public void init(FMLCommonSetupEvent event) {
		context.setActiveContainer();
		module.init(event);
	}

	public void clientInit(FMLClientSetupEvent event) {
		context.setActiveContainer();
		module.clientInit(event);
	}

	public void serverInit(FMLServerStartingEvent event) {
		context.setActiveContainer();
		module.serverInit(event);
	}

	public void postInit() {
		context.setActiveContainer();
		module.postInit();
	}

	public void gatherData(GatherDataEvent event) {
		context.setActiveContainer();
		module.gatherData(event);
	}

	public <T extends IForgeRegistryEntry<T>> List<T> getRegistries(Class<T> clazz) {
		return registries.get(clazz).stream().map($ -> $.entry).collect(Collectors.toList());
	}

}
