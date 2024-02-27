package snownee.kiwi;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.common.collect.Maps;
import com.mojang.datafixers.types.Type;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import snownee.kiwi.block.entity.InheritanceBlockEntityType;
import snownee.kiwi.loader.event.InitEvent;
import snownee.kiwi.loader.event.PostInitEvent;

/**
 * All your modules should extend {@code AbstractModule}
 *
 * @author Snownee
 */
public abstract class AbstractModule {
	protected final Map<ResourceKey<? extends Registry<?>>, BiConsumer<KiwiModuleContainer, KiwiGOHolder<?>>> decorators = Maps.newHashMap();
	public ResourceLocation uid;

	protected static <T> KiwiGO<T> go(Supplier<? extends T> factory) {
		return new KiwiGO<>((Supplier<T>) factory);
	}

	protected static <T> KiwiGO<T> go(Supplier<? extends T> factory, ResourceKey<? extends Registry<?>> registryKey) {
		return new KiwiGO.RegistrySpecified<>((Supplier<T>) factory, registryKey);
	}

	/// helper methods:
	protected static Item.Properties itemProp() {
		return new Item.Properties();
	}

	protected static BlockBehaviour.Properties blockProp() {
		return BlockBehaviour.Properties.of();
	}

	protected static BlockBehaviour.Properties blockProp(BlockBehaviour block) {
		return BlockBehaviour.Properties.ofFullCopy(block);
	}

	@SafeVarargs
	public static <T extends BlockEntity> KiwiGO<BlockEntityType<T>> blockEntity(
			FabricBlockEntityTypeBuilder.Factory<? extends T> factory,
			Type<?> datafixer,
			Supplier<? extends Block>... blocks) {
		return go(() -> FabricBlockEntityTypeBuilder.<T>create(factory, Stream.of(blocks).map(Supplier::get).toArray(Block[]::new))
				.build(datafixer));
	}

	public static <T extends BlockEntity> KiwiGO<BlockEntityType<T>> blockEntity(
			FabricBlockEntityTypeBuilder.Factory<? extends T> factory,
			Type<?> datafixer,
			Class<? extends Block> blockClass) {
		return go(() -> new InheritanceBlockEntityType<>(factory, blockClass, datafixer));
	}

	public static CreativeModeTab.Builder itemCategory(String namespace, String path, Supplier<ItemStack> icon) {
		return FabricItemGroup.builder().title(Component.translatable("itemGroup.%s.%s".formatted(namespace, path))).icon(icon);
	}

	public static TagKey<Item> itemTag(String namespace, String path) {
		return tag(Registries.ITEM, namespace, path);
	}

	public static TagKey<EntityType<?>> entityTag(String namespace, String path) {
		return tag(Registries.ENTITY_TYPE, namespace, path);
	}

	public static TagKey<Block> blockTag(String namespace, String path) {
		return tag(Registries.BLOCK, namespace, path);
	}

	public static TagKey<Fluid> fluidTag(String namespace, String path) {
		return tag(Registries.FLUID, namespace, path);
	}

	public static <T> TagKey<T> tag(ResourceKey<? extends Registry<T>> registryKey, String namespace, String path) {
		return TagKey.create(registryKey, new ResourceLocation(namespace, path));
	}

	public void addRegistries() {
	}

	protected void addEntries() {
		// NO-OP
	}

	protected void init(InitEvent event) {
		// NO-OP
	}

	protected void postInit(PostInitEvent event) {
		// NO-OP
	}

	public ResourceLocation id(String path) {
		return new ResourceLocation(uid.getNamespace(), path);
	}

	public KiwiModuleContainer container() {
		return KiwiModules.get(uid);
	}
}
