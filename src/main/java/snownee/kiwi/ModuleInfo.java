package snownee.kiwi;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.IForgeRegistryEntry;
import snownee.kiwi.item.ModBlockItem;

public class ModuleInfo
{
    static final class RegistryHolder {
        final Multimap<Class, NamedEntry<?>> registries = LinkedListMultimap.create();

        <T extends IForgeRegistryEntry<T>> void put(NamedEntry<T> entry) {
            registries.put(entry.entry.getRegistryType(), entry);
        }

        <T extends IForgeRegistryEntry<T>> Collection<NamedEntry<T>> get(Class<T> clazz) {
            return registries.get(clazz).stream().map(e -> (NamedEntry<T>) e).collect(Collectors.toList());
        }
    }

    public final AbstractModule module;
    public final ModContext context;
    public ItemGroup group;
    final RegistryHolder registries = new RegistryHolder();
    final Map<Block, Item.Properties> blockItemBuilders = Maps.newHashMap();
    final Set<Object> noGroups = Sets.newHashSet();
    final Set<Block> noItems = Sets.newHashSet();

    public ModuleInfo(ResourceLocation rl, AbstractModule module, ModContext context)
    {
        this.module = module;
        this.context = context;
        module.uid = rl;
    }

    public void register(IForgeRegistryEntry<?> entry, String name)
    {
        registries.put(new NamedEntry(name, entry));
    }

    public <T extends IForgeRegistryEntry<T>> void handleRegister(RegistryEvent.Register<T> event)
    {
        context.setActiveContainer();
        Class<T> clazz = event.getRegistry().getRegistrySuperType();
        Collection<NamedEntry<T>> entries = registries.get(clazz);
        BiConsumer<ModuleInfo, T> decorator = (BiConsumer<ModuleInfo, T>) module.decorators.getOrDefault(clazz, (a, b) -> {
        });
        if (clazz == Item.class)
        {
            registries.get(Block.class).forEach(e -> {
                if (noItems.contains(e.entry))
                    return;
                Item.Properties builder = blockItemBuilders.get(e.entry);
                if (builder == null)
                    builder = new Item.Properties();
                ModBlockItem item = new ModBlockItem(e.entry, builder);
                if (noGroups.contains(e.entry))
                    noGroups.add(item);
                entries.add(new NamedEntry(e.name, item));
            });
        } else if (clazz == Block.class && FMLEnvironment.dist.isClient()) {
            entries.stream().map(e -> (Block) e.entry).forEach(block -> {
                RenderLayer layer = null;
                Class<?> klass = block.getClass();
                while (layer == null && klass != Block.class) {
                    layer = klass.getAnnotation(RenderLayer.class);
                    klass = klass.getSuperclass();
                }
                if (layer != null) {
                    RenderTypeLookup.setRenderLayer(block, layer.value().get());
                }
            });
        }
        entries.forEach(e -> {
            decorator.accept(this, e.entry.setRegistryName(GameData.checkPrefix(e.name, true)));
            event.getRegistry().register(e.entry);
        });
    }

    public void preInit()
    {
        context.setActiveContainer();
        module.preInit();
    }

    public void init(FMLCommonSetupEvent event)
    {
        context.setActiveContainer();
        module.init(event);
    }

    public void clientInit(FMLClientSetupEvent event)
    {
        context.setActiveContainer();
        module.clientInit(event);
    }

    public void serverInit(FMLServerStartingEvent event)
    {
        context.setActiveContainer();
        module.serverInit(event);
    }

    @Deprecated
    public void serverInit(FMLDedicatedServerSetupEvent event)
    {
        context.setActiveContainer();
        module.serverInit(event);
    }

    public void postInit()
    {
        context.setActiveContainer();
        module.postInit();
    }
}
