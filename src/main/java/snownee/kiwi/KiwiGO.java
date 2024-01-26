package snownee.kiwi;

import java.util.Objects;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class KiwiGO<T> implements Supplier<T> {

	private Supplier<T> factory;
	private ResourceKey<T> key;
	private T value;

	public KiwiGO(Supplier<T> factory) {
		this.factory = factory;
	}

	@Override
	public T get() {
		Objects.requireNonNull(value);
		return value;
	}

	public T getOrCreate() {
		if (value == null) {
			value = factory.get();
			factory = null;
		}
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
		return stack.is(((ItemLike) value).asItem());
	}

	public boolean is(BlockState state) {
		if (key == null) {
			return false;
		}
		return state.is((Block) value);
	}

	public BlockState defaultBlockState() {
		return ((Block) value).defaultBlockState();
	}

	public ItemStack itemStack() {
		return itemStack(1);
	}

	public ItemStack itemStack(int amount) {
		ItemStack stack = ((ItemLike) value).asItem().getDefaultInstance();
		if (!stack.isEmpty()) {
			stack.setCount(amount);
		}
		return stack;
	}

	public ResourceLocation key() {
		return key.location();
	}

	public ResourceKey<T> resourceKey() {
		return key;
	}

	@Nullable
	public ResourceKey<? extends Registry<?>> findRegistry() {
		return Kiwi.registryLookup.findRegistry(value);
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

}
