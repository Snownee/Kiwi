package snownee.kiwi;

import java.util.Map;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;
import snownee.kiwi.block.ModBlock;

/**
 * 
 * All your modules should extend {@code AbstractModule}
 * 
 * @author Snownee
 *
 */
public abstract class AbstractModule {
    protected ResourceLocation uid;
    private static final BiConsumer<ModuleInfo, Item> ITEM_DECORATOR = (module, item) -> {
        if (module.group != null && item.group == null && !module.noGroups.contains(item))
            item.group = module.group;
    };

    private static final Map<Class, BiConsumer<ModuleInfo, ? extends IForgeRegistryEntry<?>>> DEFAULT_DECORATORS = ImmutableMap.of(Item.class, ITEM_DECORATOR);

    protected final Map<Class, BiConsumer<ModuleInfo, ? extends IForgeRegistryEntry<?>>> decorators = Maps.newHashMap(DEFAULT_DECORATORS);

    protected void preInit() {
        // NO-OP
    }

    /**
     * @author Snownee
     * @param event Note: this event's ModContainer is from Kiwi
     */
    protected void init(FMLCommonSetupEvent event) {
        // NO-OP
    }

    protected void clientInit(FMLClientSetupEvent event) {
        // NO-OP
    }

    protected void serverInit(FMLServerStartingEvent event) {
        // NO-OP
    }

    protected void postInit() {
        // NO-OP
    }

    /// helper methods:
    protected static Item.Properties itemProp() {
        return new Item.Properties();
    }

    protected static Block.Properties blockProp(Material material) {
        return Block.Properties.create(material);
    }

    /**
     * @since 2.5.2
     */
    protected static Block.Properties blockProp(Block block) {
        return Block.Properties.from(block);
    }

    protected static <T extends Block> T init(T block) {
        return ModBlock.deduceSoundAndHardness(block);
    }

    public static Tag<Item> itemTag(String namespace, String path) {
        return new ItemTags.Wrapper(new ResourceLocation(namespace, path));
    }

    public static Tag<EntityType<?>> entityTag(String namespace, String path) {
        return new EntityTypeTags.Wrapper(new ResourceLocation(namespace, path));
    }

    public static Tag<Block> blockTag(String namespace, String path) {
        return new BlockTags.Wrapper(new ResourceLocation(namespace, path));
    }

    public static Tag<Fluid> fluidTag(String namespace, String path) {
        return new FluidTags.Wrapper(new ResourceLocation(namespace, path));
    }

    /**
     * @since 2.6.0
     */
    public ResourceLocation RL(String path) {
        return new ResourceLocation(uid.getNamespace(), path);
    }
}
