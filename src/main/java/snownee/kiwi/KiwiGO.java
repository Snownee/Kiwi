package snownee.kiwi;

import java.util.Objects;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class KiwiGO<T> implements Supplier<T> {

	private Supplier<T> factory;
	private ResourceLocation key;
	private T value;

	public KiwiGO(Supplier<T> factory) {
		this.factory = factory;
	}

	@Override
	public T get() {
		Objects.requireNonNull(value);
		return value;
	}

	public T create(ResourceLocation key) {
		this.key = key;
		value = factory.get();
		return get();
	}

	public boolean is(Object value) {
		return Objects.equals(this.value, value);
	}

	public boolean is(ItemStack stack) {
		return stack.is(((ItemLike) value).asItem());
	}

	public boolean is(BlockState state) {
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
		return key;
	}

	@Nullable
	public Object registry() {
		return Kiwi.registryLookup.findRegistry(value);
	}

	public static class RegistrySpecified<T> extends KiwiGO<T> {

		final Supplier<Object> registry;

		public RegistrySpecified(Supplier<T> factory, Supplier<Object> registry) {
			super(factory);
			this.registry = registry;
		}

		@Override
		public Object registry() {
			return registry.get();
		}

	}

}
