package snownee.kiwi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import snownee.kiwi.crafting.NoContainersShapedRecipe;
import snownee.kiwi.crafting.NoContainersShapelessRecipe;

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

    @SubscribeEvent
    public static void registerEffects(RegistryEvent.Register<Effect> event)
    {
        MODULES.values().forEach(info -> info.registerEffects(event));
        ModLoadingContext.get().setActiveContainer(null, null);
    }

    @SubscribeEvent
    public static void registerPotions(RegistryEvent.Register<Potion> event)
    {
        MODULES.values().forEach(info -> info.registerPotions(event));
        ModLoadingContext.get().setActiveContainer(null, null);
    }

    @SubscribeEvent
    public static void registerTiles(RegistryEvent.Register<TileEntityType<?>> event)
    {
        MODULES.values().forEach(info -> info.registerTiles(event));
        ModLoadingContext.get().setActiveContainer(null, null);
    }

    @SubscribeEvent
    public static void registerRecipeTypes(RegistryEvent.Register<IRecipeSerializer<?>> event)
    {
        event.getRegistry().register(new NoContainersShapedRecipe.Serializer().setRegistryName(Kiwi.MODID, "shaped_no_containers"));
        event.getRegistry().register(new NoContainersShapelessRecipe.Serializer().setRegistryName(Kiwi.MODID, "shapeless_no_containers"));

        MODULES.values().forEach(info -> info.registerRecipeTypes(event));
        ModLoadingContext.get().setActiveContainer(null, null);
    }

    @SubscribeEvent
    public static void registerEntityTypes(RegistryEvent.Register<EntityType<?>> event)
    {
        MODULES.values().forEach(info -> info.registerEntityTypes(event));
        ModLoadingContext.get().setActiveContainer(null, null);
    }

}
