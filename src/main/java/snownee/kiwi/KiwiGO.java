package snownee.kiwi;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class KiwiGO<T> implements Supplier<T> {

	@Nullable
	protected Supplier<T> factory;
	@Nullable
	protected ResourceKey<T> key;
	@Nullable
	protected T value;
	@Nullable Field field;
	@Nullable GroupSetting groupSetting;

	public KiwiGO(@Nullable Supplier<T> factory) {
		this.factory = factory;
	}

	@Override
	public T get() {
		Objects.requireNonNull(value);
		return value;
	}

	public T getOrCreate() {
		if (value == null) {
			Objects.requireNonNull(factory);
			value = Objects.requireNonNull(factory.get());
			factory = null;
		}
		return get();
	}

	@Nullable
	public T preRegister(ResourceLocation id) {
		getOrCreate();
		ResourceKey<? extends Registry<?>> registryKey = findRegistry();
		//noinspection unchecked,rawtypes
		ResourceKey resourceKey = ResourceKey.create((ResourceKey) registryKey, id);
		//noinspection unchecked
		setKey(resourceKey);
		return get();
	}

	public void setKey(ResourceKey<T> key) {
		Objects.requireNonNull(key);
		if (this.key != null) {
			throw new IllegalStateException("Key already set: " + this.key + " -> " + key);
		}
		this.key = key;
	}

	public boolean is(Object value) {
		if (key == null) {
			return false;
		}
		return Objects.equals(this.value, value);
	}

	public boolean is(ItemStack stack) {
		if (key == null || stack.isEmpty()) {
			return false;
		}
		return stack.is(((ItemLike) get()).asItem());
	}

	public boolean is(BlockState state) {
		if (key == null) {
			return false;
		}
		return state.is((Block) get());
	}

	public BlockState defaultBlockState() {
		return ((Block) get()).defaultBlockState();
	}

	public ItemStack itemStack() {
		return itemStack(1);
	}

	public ItemStack itemStack(int amount) {
		ItemStack stack = ((ItemLike) get()).asItem().getDefaultInstance();
		if (!stack.isEmpty()) {
			stack.setCount(amount);
		}
		return stack;
	}

	public ResourceLocation key() {
		return resourceKey().location();
	}

	public ResourceKey<T> resourceKey() {
		return Objects.requireNonNull(key);
	}

	@Nullable
	public Field field() {
		return field;
	}

	@Nullable
	public GroupSetting groupSetting() {
		return groupSetting;
	}

	void register() {
		//noinspection unchecked
		Registry<T> registry = (Registry<T>) Objects.requireNonNull(BuiltInRegistries.REGISTRY.getValue(resourceKey().registry()));
		Registry.register(registry, key(), get());
	}

	@Nullable
	public ResourceKey<? extends Registry<?>> findRegistry() {
		return Kiwi.registryLookup.findRegistry(get());
	}

	public Optional<? extends Holder<T>> holder() {
		if (key == null) {
			return Optional.empty();
		}
		//noinspection unchecked
		Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.getValue(key.registry());
		if (registry == null) {
			return Optional.empty();
		}
		return registry.get(key);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("key", key).append("value", value).append("field", field).append(
				"groupSetting",
				groupSetting).toString();
	}

	public static class RegistrySpecified<T> extends KiwiGO<T> {
		final ResourceKey<? extends Registry<?>> registryKey;

		public RegistrySpecified(Supplier<T> factory, ResourceKey<? extends Registry<?>> registryKey) {
			super(factory);
			this.registryKey = registryKey;
		}

		@Override
		public ResourceKey<? extends Registry<?>> findRegistry() {
			return registryKey;
		}
	}

	public static class Direct<T> extends KiwiGO<T> {
		public Direct(T value) {
			super(null);
			this.value = Objects.requireNonNull(value);
		}
	}

	public static class Ref<T> extends KiwiGO<T> {
		final ResourceKey<? extends Registry<?>> registryKey;

		public Ref(ResourceKey<? extends Registry<?>> registryKey) {
			super(null);
			this.registryKey = registryKey;
		}

		@Override
		@Nullable
		public T preRegister(ResourceLocation id) {
			//noinspection unchecked
			setKey(ResourceKey.create((ResourceKey<? extends Registry<T>>) registryKey, id));
			return null;
		}

		@Override
		public ResourceKey<? extends Registry<?>> findRegistry() {
			return registryKey;
		}

		@Override
		public T get() {
			if (value == null) {
				//noinspection unchecked
				Registry<T> registry = (Registry<T>) Objects.requireNonNull(BuiltInRegistries.REGISTRY.getValue(resourceKey().registry()));
				value = registry.getValueOrThrow(resourceKey());
			}
			return value;
		}

		@Override
		public T getOrCreate() {
			return get();
		}
	}

}
