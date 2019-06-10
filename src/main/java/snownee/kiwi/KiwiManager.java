package snownee.kiwi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import snownee.kiwi.item.ModBlockItem;

@EventBusSubscriber(modid = Kiwi.MODID, bus = Bus.MOD)
public class KiwiManager
{
    public static final HashMap<ResourceLocation, ModuleInfo> MODULES = Maps.newHashMap();
    public static final HashSet<ResourceLocation> ENABLED_MODULES = Sets.newHashSet();
    static Map<String, ItemGroup> GROUPS = Maps.newHashMap();

    private KiwiManager()
    {
    }

    public static void addInstance(ResourceLocation resourceLocation, AbstractModule module, ModContext context)
    {
        if (MODULES.containsKey(resourceLocation))
        {
            Kiwi.logger.error("Found a duplicate module name, skipping.");
        }
        else
        {
            MODULES.put(resourceLocation, new ModuleInfo(resourceLocation, module, context));
            ENABLED_MODULES.add(resourceLocation);
        }
    }

    public static void addItemGroup(String modId, String name, ItemGroup group)
    {
        GROUPS.put(modId + ":" + name, group);
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {
        MODULES.values().forEach(info -> info.registerBlocks(event));
        ModLoadingContext.get().setActiveContainer(null, null);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        MODULES.values().forEach(info -> info.registerItems(event));
        ModLoadingContext.get().setActiveContainer(null, null);
    }

    //    @SubscribeEvent
    //    public static void registerPotions(RegistryEvent.Register<Potion> event)
    //    {
    //        POTIONS.forEach((potion, rl) -> {
    //            CONTEXTS.get(rl.getNamespace()).setActiveContainer();
    //            //potion.register(modid);
    //            event.getRegistry().register(potion.setRegistryName(rl));
    //        });
    //        ModLoadingContext.get().setActiveContainer(null, null);
    //    }

    //    @SubscribeEvent
    //    public static void registerPotionEffects(RegistryEvent.Register<PotionType> event)
    //    {
    //        Map<String, ModContainer> map = Loader.instance().getIndexedModList();
    //        POTIONS.forEach((potion, modid) -> {
    //            Loader.instance().setActiveModContainer(map.get(modid));
    //            Collection<PotionType> types = potion.getPotionTypes();
    //            for (PotionType type : types)
    //            {
    //                event.getRegistry().register(type.setRegistryName(modid, type.getNamePrefixed("")));
    //            }
    //        });
    //        Loader.instance().setActiveModContainer(null);
    //    }

}
