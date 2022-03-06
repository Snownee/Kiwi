package snownee.kiwi;

import java.util.Objects;
import java.util.function.Supplier;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class KiwiGO<T> implements Supplier<T> {

	private Supplier<T> factory;
	private T value;

	public KiwiGO(Supplier<T> factory) {
		this.factory = factory;
	}

	@Override
	public T get() {
		Objects.requireNonNull(value);
		return value;
	}

	public T create() {
		value = factory.get();
		return get();
	}

	public boolean is(T value) {
		return Objects.equals(this.value, value);
	}

	public boolean is(ItemStack stack) {
		if (value instanceof Item) {
			return stack.is((Item) value);
		}
		return false;
	}

	public boolean is(BlockState state) {
		if (value instanceof Block) {
			return state.is((Block) value);
		}
		return false;
	}

}
