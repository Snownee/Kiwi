package snownee.kiwi;

import java.util.Map;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fmlserverevents.FMLServerStartingEvent;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
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
	public ResourceLocation uid;
	private static final BiConsumer<ModuleInfo, Item> ITEM_DECORATOR = (module, item) -> {
		if (module.category != null && item.category == null && !module.noCategories.contains(item))
			item.category = module.category;
	};
	private static final BiConsumer<ModuleInfo, Block> BLOCK_DECORATOR = (module, block) -> {
		ModBlock.setFireInfo(block);
	};

	private static final Map<Class<?>, BiConsumer<ModuleInfo, ? extends IForgeRegistryEntry<?>>> DEFAULT_DECORATORS = ImmutableMap.of(Item.class, ITEM_DECORATOR, Block.class, BLOCK_DECORATOR);

	protected final Map<Class<?>, BiConsumer<ModuleInfo, ? extends IForgeRegistryEntry<?>>> decorators = Maps.newHashMap(DEFAULT_DECORATORS);

	protected void preInit() {
		// NO-OP
	}

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

	/**
	 * @since 4.1.0
	 */
	protected void gatherData(GatherDataEvent event) {
		// NO-OP
	}

	/// helper methods:
	protected static Item.Properties itemProp() {
		return new Item.Properties();
	}

	protected static BlockBehaviour.Properties blockProp(Material material) {
		BlockBehaviour.Properties properties = BlockBehaviour.Properties.of(material);
		properties.sound(ModBlock.deduceSoundType(material));
		properties.strength(ModBlock.deduceHardness(material));
		return properties;
	}

	/**
	 * @since 2.5.2
	 */
	protected static BlockBehaviour.Properties blockProp(BlockBehaviour block) {
		return BlockBehaviour.Properties.copy(block);
	}

	public static Named<Item> itemTag(String namespace, String path) {
		return ItemTags.bind(namespace + ":" + path);
	}

	public static Named<EntityType<?>> entityTag(String namespace, String path) {
		return EntityTypeTags.bind(namespace + ":" + path);
	}

	public static Named<Block> blockTag(String namespace, String path) {
		return BlockTags.bind(namespace + ":" + path);
	}

	public static Named<Fluid> fluidTag(String namespace, String path) {
		return FluidTags.bind(namespace + ":" + path);
	}

	/**
	 * @since 2.6.0
	 */
	public ResourceLocation RL(String path) {
		return new ResourceLocation(uid.getNamespace(), path);
	}
}
