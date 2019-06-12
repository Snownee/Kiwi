package snownee.kiwi;

import java.util.Map;
import java.util.Set;

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
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import snownee.kiwi.item.ModBlockItem;

public class ModuleInfo
{
    public final ResourceLocation rl;
    public final AbstractModule module;
    public final ModContext context;
    public ItemGroup group;
    final Map<Block, String> blocks = Maps.newLinkedHashMap();
    final Map<Item, String> items = Maps.newLinkedHashMap();
    final Map<Effect, String> effects = Maps.newLinkedHashMap();
    final Map<Potion, String> potions = Maps.newLinkedHashMap();
    final Map<TileEntityType<?>, String> tileTypes = Maps.newLinkedHashMap();
    final Map<IRecipeSerializer<?>, String> recipeTypes = Maps.newLinkedHashMap();
    final Map<EntityType<?>, String> entityTypes = Maps.newLinkedHashMap();
    final Map<Block, Item.Properties> blockItemBuilders = Maps.newHashMap();
    final Set<Object> noGroups = Sets.newHashSet();
    final Set<Block> noItems = Sets.newHashSet();

    public ModuleInfo(ResourceLocation rl, AbstractModule module, ModContext context)
    {
        this.rl = rl;
        this.module = module;
        this.context = context;
    }

    public void registerBlocks(RegistryEvent.Register<Block> event)
    {
        context.setActiveContainer();
        blocks.forEach((block, name) -> {
            event.getRegistry().register(block.setRegistryName(new ResourceLocation(rl.getNamespace(), name)));
        });
    }

    public void registerItems(RegistryEvent.Register<Item> event)
    {
        context.setActiveContainer();
        items.forEach((item, name) -> {
            if (group != null && item.group == null && !noGroups.contains(item))
                item.group = group;
            event.getRegistry().register(item.setRegistryName(new ResourceLocation(rl.getNamespace(), name)));
        });
        blocks.forEach((block, name) -> {
            if (noItems.contains(block))
                return;
            Item.Properties builder = blockItemBuilders.get(block);
            if (builder == null)
                builder = new Item.Properties();
            ModBlockItem item = new ModBlockItem(block, builder);
            if (group != null && builder.group == null && !noGroups.contains(item))
                item.group = group;
            event.getRegistry().register(item.setRegistryName(block.getRegistryName()));
        });
    }

    public void registerEffects(RegistryEvent.Register<Effect> event)
    {
        context.setActiveContainer();
        effects.forEach((effect, name) -> {
            event.getRegistry().register(effect.setRegistryName(new ResourceLocation(rl.getNamespace(), name)));
        });
    }

    public void registerPotions(RegistryEvent.Register<Potion> event)
    {
        context.setActiveContainer();
        potions.forEach((potion, name) -> {
            event.getRegistry().register(potion.setRegistryName(new ResourceLocation(rl.getNamespace(), name)));
        });
    }

    public void registerTiles(RegistryEvent.Register<TileEntityType<?>> event)
    {
        context.setActiveContainer();
        tileTypes.forEach((tileType, name) -> {
            event.getRegistry().register(tileType.setRegistryName(new ResourceLocation(rl.getNamespace(), name)));
        });
    }

    public void registerRecipeTypes(Register<IRecipeSerializer<?>> event)
    {
        context.setActiveContainer();
        recipeTypes.forEach((recipeType, name) -> {
            event.getRegistry().register(recipeType.setRegistryName(new ResourceLocation(rl.getNamespace(), name)));
        });
    }

    public void registerEntityTypes(Register<EntityType<?>> event)
    {
        context.setActiveContainer();
        entityTypes.forEach((entityType, name) -> {
            event.getRegistry().register(entityType.setRegistryName(new ResourceLocation(rl.getNamespace(), name)));
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
