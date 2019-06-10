package snownee.kiwi;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import snownee.kiwi.item.ModBlockItem;

public class ModuleInfo
{
    public final ResourceLocation rl;
    public final AbstractModule module;
    public final ModContext context;
    public ItemGroup group;
    final Map<Block, String> blocks = Maps.newLinkedHashMap();
    final Map<Item, String> items = Maps.newLinkedHashMap();
    //    public static Map<PotionMod, ResourceLocation> potions = Maps.newLinkedHashMap();
    final Map<Block, Item.Properties> blockItemBuilders = Maps.newHashMap();
    final Set<Object> noGroups = Sets.newHashSet();

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
            if (!noGroups.contains(item))
                item.group = group;
            event.getRegistry().register(item.setRegistryName(new ResourceLocation(rl.getNamespace(), name)));
        });
        blocks.forEach((block, name) -> {
            // TODO: @DontRegisterItem
            Item.Properties builder = blockItemBuilders.get(block);
            if (builder == null)
                builder = new Item.Properties();
            ModBlockItem item = new ModBlockItem(block, builder);
            if (!noGroups.contains(item))
                item.group = group;
            event.getRegistry().register(item.setRegistryName(block.getRegistryName()));
        });
    }

    public void preInit()
    {
        context.setActiveContainer();
        module.preInit();
    }
}
