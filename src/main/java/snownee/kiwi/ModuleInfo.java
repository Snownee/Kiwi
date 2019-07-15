package snownee.kiwi;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;
import snownee.kiwi.item.ModBlockItem;

public class ModuleInfo
{
    public final ResourceLocation rl;
    public final AbstractModule module;
    public final ModContext context;
    public ItemGroup group;
    final Multimap<Class, NamedEntry> registries = LinkedListMultimap.create();
    final Map<Block, Item.Properties> blockItemBuilders = Maps.newHashMap();
    final Set<Object> noGroups = Sets.newHashSet();
    final Set<Block> noItems = Sets.newHashSet();

    public ModuleInfo(ResourceLocation rl, AbstractModule module, ModContext context)
    {
        this.rl = rl;
        this.module = module;
        this.context = context;
    }

    public void register(IForgeRegistryEntry<?> entry, String name)
    {
        registries.put(entry.getRegistryType(), new NamedEntry(name, entry));
    }

    public <T extends IForgeRegistryEntry<T>> void handleRegister(RegistryEvent.Register<T> event)
    {
        context.setActiveContainer();
        Class<?> clazz = event.getRegistry().getRegistrySuperType();
        Collection<NamedEntry> entries = registries.get(clazz);
        BiConsumer<ModuleInfo, IForgeRegistryEntry<?>> decorator = module.decorators.getOrDefault(clazz, (a, b) -> {
        });
        if (clazz == Item.class)
        {
            registries.get(Block.class).forEach(e -> {
                if (noItems.contains(e.entry))
                    return;
                Item.Properties builder = blockItemBuilders.get(e.entry);
                if (builder == null)
                    builder = new Item.Properties();
                ModBlockItem item = new ModBlockItem((Block) e.entry, builder);
                if (noGroups.contains(e.entry))
                    noGroups.add(item);
                entries.add(new NamedEntry(e.name, item));
            });
        }
        entries.forEach(e -> {
            decorator.accept(this, (T) e.entry.setRegistryName(new ResourceLocation(rl.getNamespace(), e.name)));
            event.getRegistry().register((T) e.entry);
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
