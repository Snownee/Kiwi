package snownee.kiwi;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import snownee.kiwi.block.IModBlock;
import snownee.kiwi.item.IModItem;
import snownee.kiwi.item.ItemModBlock;
import snownee.kiwi.potion.PotionMod;

@EventBusSubscriber(modid = Kiwi.MODID)
public class KiwiManager
{
    public static final HashMap<ResourceLocation, IModule> MODULES = new HashMap<>();
    public static final HashSet<ResourceLocation> ENABLED_MODULES = new HashSet<>();
    public static Map<IModBlock, String> BLOCKS = new HashMap<>();
    public static Map<IModItem, String> ITEMS = new HashMap<>();
    public static Map<PotionMod, String> POTIONS = new HashMap<>();

    private KiwiManager()
    {
    }

    public static void addInstance(ResourceLocation resourceLocation, IModule module)
    {
        if (MODULES.containsKey(resourceLocation))
        {
            Kiwi.logger.error("Found a duplicate module name, skipping.");
        }
        else
        {
            MODULES.put(resourceLocation, module);
            ENABLED_MODULES.add(resourceLocation);
        }
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {
        Map<String, ModContainer> map = Loader.instance().getIndexedModList();
        BLOCKS.forEach((block, modid) -> {
            Loader.instance().setActiveModContainer(map.get(modid));
            block.register(modid);
            event.getRegistry().register(block.cast());
        });
        Loader.instance().setActiveModContainer(null);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        Map<String, ModContainer> map = Loader.instance().getIndexedModList();
        ITEMS.forEach((item, modid) -> {
            Loader.instance().setActiveModContainer(map.get(modid));
            item.register(modid);
            event.getRegistry().register(item.cast());
        });
        BLOCKS.forEach((block, modid) -> {
            if (block.getItemSubtypeAmount() > 0)
            {
                Loader.instance().setActiveModContainer(map.get(modid));
                ItemModBlock item = new ItemModBlock(block);
                item.setRegistryName(block.getRegistryName());
                event.getRegistry().register(item);
            }
        });
        Loader.instance().setActiveModContainer(null);
    }

    @SubscribeEvent
    public static void registerPotions(RegistryEvent.Register<Potion> event)
    {
        Map<String, ModContainer> map = Loader.instance().getIndexedModList();
        POTIONS.forEach((potion, modid) -> {
            Loader.instance().setActiveModContainer(map.get(modid));
            potion.register(modid);
            event.getRegistry().register(potion);
        });
        Loader.instance().setActiveModContainer(null);
    }

    @SubscribeEvent
    public static void registerPotionEffects(RegistryEvent.Register<PotionType> event)
    {
        Map<String, ModContainer> map = Loader.instance().getIndexedModList();
        POTIONS.forEach((potion, modid) -> {
            Loader.instance().setActiveModContainer(map.get(modid));
            Collection<PotionType> types = potion.getPotionTypes();
            for (PotionType type : types)
            {
                event.getRegistry().register(type.setRegistryName(modid, type.getNamePrefixed("")));
            }
        });
        Loader.instance().setActiveModContainer(null);
    }

}
