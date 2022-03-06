package snownee.kiwi;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.types.Type;

import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;
import snownee.kiwi.block.ModBlock;
import snownee.kiwi.loader.event.ClientInitEvent;
import snownee.kiwi.loader.event.InitEvent;
import snownee.kiwi.loader.event.PostInitEvent;
import snownee.kiwi.loader.event.ServerInitEvent;
import snownee.kiwi.mixin.ItemAccess;

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
		if (module.category != null && ((ItemAccess) item).getCategory() == null && !module.noCategories.contains(item))
			((ItemAccess) item).setCategory(module.category);
	};
	private static final BiConsumer<ModuleInfo, Block> BLOCK_DECORATOR = (module, block) -> {
		ModBlock.setFireInfo(block);
	};

	private static final Map<Class<?>, BiConsumer<ModuleInfo, ? extends IForgeRegistryEntry<?>>> DEFAULT_DECORATORS = ImmutableMap.of(Item.class, ITEM_DECORATOR, Block.class, BLOCK_DECORATOR);

	protected final Map<Class<?>, BiConsumer<ModuleInfo, ? extends IForgeRegistryEntry<?>>> decorators = Maps.newHashMap(DEFAULT_DECORATORS);

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

	/**
	 * @since 4.1.0
	 */
	protected void gatherData(GatherDataEvent event) {
		// NO-OP
	}

	protected static <T> KiwiGO<T> go(Supplier<? extends T> factory) {
		return new KiwiGO<>((Supplier<T>) factory);
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

	/**
	* @since 5.2.0
	*/
	public static <T extends BlockEntity> KiwiGO<BlockEntityType<T>> blockEntity(BlockEntitySupplier<? extends T> factory, Type<?> datafixer, Supplier<? extends Block>... blocks) {
		return go(() -> BlockEntityType.Builder.<T>of(factory, Stream.of(blocks).map(Supplier::get).toArray(Block[]::new)).build(datafixer));
	}

	/**
	 * @since 5.2.0
	 */
	public static CreativeModeTab itemCategory(String namespace, String path, Supplier<ItemStack> icon, @Nullable BiConsumer<List<ItemStack>, CreativeModeTab> stacksForDisplay) {
		return new CreativeModeTab(namespace + "." + path) {
			@Override
			public ItemStack makeIcon() {
				return icon.get();
			}

			@Override
			public void fillItemList(NonNullList<ItemStack> stacks) {
				if (stacksForDisplay != null) {
					stacksForDisplay.accept(stacks, this);
					return;
				}
				super.fillItemList(stacks);
			}
		};
	}

	public static TagKey<Item> itemTag(String namespace, String path) {
		return tag(Registry.ITEM_REGISTRY, namespace, path);
	}

	public static TagKey<EntityType<?>> entityTag(String namespace, String path) {
		return tag(Registry.ENTITY_TYPE_REGISTRY, namespace, path);
	}

	public static TagKey<Block> blockTag(String namespace, String path) {
		return tag(Registry.BLOCK_REGISTRY, namespace, path);
	}

	public static TagKey<Fluid> fluidTag(String namespace, String path) {
		return tag(Registry.FLUID_REGISTRY, namespace, path);
	}

	public static <T> TagKey<T> tag(ResourceKey<? extends Registry<T>> registryKey, String namespace, String path) {
		return TagKey.create(registryKey, new ResourceLocation(namespace, path));
	}

	/**
	 * @since 2.6.0
	 */
	public ResourceLocation RL(String path) {
		return new ResourceLocation(uid.getNamespace(), path);
	}
}
