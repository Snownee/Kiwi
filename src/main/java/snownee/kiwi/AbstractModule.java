package snownee.kiwi;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.types.Type;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.Factory;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import snownee.kiwi.block.ModBlock;
import snownee.kiwi.loader.event.ClientInitEvent;
import snownee.kiwi.loader.event.InitEvent;
import snownee.kiwi.loader.event.PostInitEvent;
import snownee.kiwi.loader.event.ServerInitEvent;
import snownee.kiwi.mixin.ItemAccessor;

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
		if (module.category != null && ((ItemAccessor) item).getCategory() == null && !module.noCategories.contains(item))
			((ItemAccessor) item).setCategory(module.category);
	};
	private static final BiConsumer<ModuleInfo, Block> BLOCK_DECORATOR = (module, block) -> {
		ModBlock.setFireInfo(block);
	};

	private static final Map<Registry<?>, BiConsumer<ModuleInfo, ?>> DEFAULT_DECORATORS = ImmutableMap.of(Registry.ITEM, ITEM_DECORATOR, Registry.BLOCK, BLOCK_DECORATOR);

	protected final Map<Registry<?>, BiConsumer<ModuleInfo, ?>> decorators = Maps.newHashMap(DEFAULT_DECORATORS);

	protected void preInit() {
		// NO-OP
	}

	protected void init(InitEvent event) {
		// NO-OP
	}

	protected void clientInit(ClientInitEvent event) {
		// NO-OP
	}

	protected void serverInit(ServerInitEvent event) {
		// NO-OP
	}

	protected void postInit(PostInitEvent event) {
		// NO-OP
	}

	//	/**
	//	 * @since 4.1.0
	//	 */
	//	protected void gatherData(GatherDataEvent event) {
	//		// NO-OP
	//	}

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

	/**
	 * @since 5.2.0
	 */
	public static <T extends BlockEntity> BlockEntityType<T> blockEntity(Factory<? extends T> factory, Type<?> datafixer, Block... blocks) {
		return FabricBlockEntityTypeBuilder.<T>create(factory, blocks).build(datafixer);
	}

	/**
	 * @since 5.2.0
	 */
	public static CreativeModeTab itemCategory(String namespace, String path, Supplier<ItemStack> icon, @Nullable BiConsumer<List<ItemStack>, CreativeModeTab> stacksForDisplay) {
		return FabricItemGroupBuilder.create(new ResourceLocation(namespace, path)).icon(icon).appendItems(stacksForDisplay).build();
	}

	public static Named<Item> itemTag(String namespace, String path) {
		return TagFactory.ITEM.create(new ResourceLocation(namespace, path));
	}

	public static Named<EntityType<?>> entityTag(String namespace, String path) {
		return TagFactory.ENTITY_TYPE.create(new ResourceLocation(namespace, path));
	}

	public static Named<Block> blockTag(String namespace, String path) {
		return TagFactory.BLOCK.create(new ResourceLocation(namespace, path));
	}

	public static Named<Fluid> fluidTag(String namespace, String path) {
		return TagFactory.FLUID.create(new ResourceLocation(namespace, path));
	}

	/**
	 * @since 2.6.0
	 */
	public ResourceLocation RL(String path) {
		return new ResourceLocation(uid.getNamespace(), path);
	}
}
