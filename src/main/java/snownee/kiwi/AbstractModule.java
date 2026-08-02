package snownee.kiwi;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
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
import snownee.kiwi.util.KiwiTabBuilder;

/**
 * All your modules should extend {@code AbstractModule}
 *
 * @author Snownee
 */
public abstract class AbstractModule {
	private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
	protected final Map<ResourceKey<? extends Registry<?>>, BiConsumer<KiwiModuleContainer, KiwiGO<?>>> decorators = Maps.newIdentityHashMap();
	public @Nullable Identifier uid;

	protected static <T> KiwiGO<T> go(Supplier<? extends T> factory) {
		//noinspection unchecked
		return new KiwiGO<>((Supplier<T>) factory);
	}

	protected static <T> KiwiGO<T> go(Supplier<? extends T> factory, ResourceKey<? extends Registry<?>> registryKey) {
		//noinspection unchecked
		return new KiwiGO.RegistrySpecified<>((Supplier<T>) factory, registryKey);
	}

	protected static <T, U> KiwiGO<T> go(ResourceKey<Registry<U>> registryKey, Function<ResourceKey<U>, ? extends T> factory) {
		//noinspection unchecked
		return new KiwiGO.Keyed<>(registryKey, (Function<ResourceKey<U>, T>) factory);
	}

	protected static <T extends Item> ItemObject<T> item(Function<Item.Properties, T> factory) {
		return new ItemObject<>(factory);
	}

	protected static <T extends Block> BlockObject<T> block(Function<BlockBehaviour.Properties, T> factory) {
		return block(factory, null);
	}

	protected static <T extends Block> BlockObject<T> block(
			Function<BlockBehaviour.Properties, T> factory,
			@Nullable Supplier<Block> copyFrom) {
		return new BlockObject<>(factory, copyFrom);
	}

	protected static <T> KiwiGO<T> ref(ResourceKey<? extends Registry<?>> registryKey) {
		return new KiwiGO.Ref<>(registryKey);
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
			BlockEntityType.BlockEntitySupplier<? extends T> factory,
			Supplier<? extends Block>... blocks) {
		return blockEntity(factory, false, blocks);
	}

	public static <T extends Entity, U extends Entity> KiwiGO<EntityType<U>> entity(Function<ResourceKey<EntityType<?>>, ? extends EntityType<T>> factory) {
		//noinspection unchecked
		return (KiwiGO<EntityType<U>>) (Object) go(Registries.ENTITY_TYPE, factory);
	}

	@SafeVarargs
	public static <T extends BlockEntity> KiwiGO<BlockEntityType<T>> blockEntity(
			BlockEntityType.BlockEntitySupplier<? extends T> factory,
			boolean onlyOpCanSetNbt,
			Supplier<? extends Block>... blocks) {
		return go(() -> new BlockEntityType<>(
				factory,
				Stream.of(blocks).map(Supplier::get).collect(Collectors.toSet())) {
			@Override
			public boolean onlyOpCanSetNbt() {
				return onlyOpCanSetNbt;
			}
		});
	}

	public static <T extends BlockEntity> KiwiGO<BlockEntityType<T>> blockEntity(
			BlockEntityType.BlockEntitySupplier<? extends T> factory,
			Class<?> blockClass) {
		return blockEntity(factory, false, blockClass);
	}

	public static <T extends BlockEntity> KiwiGO<BlockEntityType<T>> blockEntity(
			BlockEntityType.BlockEntitySupplier<? extends T> factory,
			boolean onlyOpCanSetNbt,
			Class<?> blockClass) {
		return go(() -> new InheritanceBlockEntityType<>(factory, blockClass, onlyOpCanSetNbt));
	}

	public static CreativeModeTab.Builder itemCategory(Identifier id, Supplier<ItemStack> icon) {
		return new KiwiTabBuilder(id).icon(icon);
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
		return TagKey.create(registryKey, Identifier.fromNamespaceAndPath(namespace, path));
	}

	public static TagKey<Item> itemTag(String id) {
		return tag(Registries.ITEM, id);
	}

	public static TagKey<EntityType<?>> entityTag(String id) {
		return tag(Registries.ENTITY_TYPE, id);
	}

	public static TagKey<Block> blockTag(String id) {
		return tag(Registries.BLOCK, id);
	}

	public static TagKey<Fluid> fluidTag(String id) {
		return tag(Registries.FLUID, id);
	}

	public static <T> TagKey<T> tag(ResourceKey<? extends Registry<T>> registryKey, String id) {
		Identifier location;
		if (id.contains(":")) {
			location = Identifier.parse(id);
		} else {
			Class<?> callerClass = STACK_WALKER.walk(stream -> stream
					.map(StackWalker.StackFrame::getDeclaringClass)
					.filter(cls -> cls != AbstractModule.class)
					.findFirst()
			).orElse(null);
			if (callerClass == null) {
				throw new IllegalStateException("No caller class found");
			}
			KiwiModule annotation = callerClass.getDeclaredAnnotation(KiwiModule.class);
			if (annotation == null || annotation.modId().isEmpty()) {
				throw new IllegalStateException("No KiwiModule modId found on " + callerClass.getName());
			}
			location = Identifier.fromNamespaceAndPath(annotation.modId(), id);
		}
		return TagKey.create(registryKey, location);
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

	public Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(Objects.requireNonNull(uid).getNamespace(), path);
	}

	public KiwiModuleContainer container() {
		return KiwiModules.get(Objects.requireNonNull(uid));
	}
}
