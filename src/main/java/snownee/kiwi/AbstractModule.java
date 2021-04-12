package snownee.kiwi;

import java.util.Map;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.tags.ItemTags;
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
    private static final BiConsumer<ModuleInfo, Block> BLOCK_DECORATOR = (module, block) -> {
        ModBlock.setFireInfo(block);
    };

    private static final Map<Class<?>, BiConsumer<ModuleInfo, ? extends IForgeRegistryEntry<?>>> DEFAULT_DECORATORS = ImmutableMap.of(Item.class, ITEM_DECORATOR, Block.class, BLOCK_DECORATOR);

    protected final Map<Class<?>, BiConsumer<ModuleInfo, ? extends IForgeRegistryEntry<?>>> decorators = Maps.newHashMap(DEFAULT_DECORATORS);

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

    protected static AbstractBlock.Properties blockProp(Material material) {
        AbstractBlock.Properties properties = AbstractBlock.Properties.create(material);
        properties.sound(ModBlock.deduceSoundType(material));
        properties.hardnessAndResistance(ModBlock.deduceHardness(material));
        return properties;
    }

    /**
     * @since 2.5.2
     */
    protected static AbstractBlock.Properties blockProp(AbstractBlock block) {
        return AbstractBlock.Properties.from(block);
    }

    public static INamedTag<Item> itemTag(String namespace, String path) {
        return ItemTags.makeWrapperTag(namespace + ":" + path);
    }

    public static INamedTag<EntityType<?>> entityTag(String namespace, String path) {
        return EntityTypeTags.getTagById(namespace + ":" + path);
    }

    public static INamedTag<Block> blockTag(String namespace, String path) {
        return BlockTags.makeWrapperTag(namespace + ":" + path);
    }

    public static INamedTag<Fluid> fluidTag(String namespace, String path) {
        return FluidTags.makeWrapperTag(namespace + ":" + path);
    }

    /**
     * @since 2.6.0
     */
    public ResourceLocation RL(String path) {
        return new ResourceLocation(uid.getNamespace(), path);
    }
}
