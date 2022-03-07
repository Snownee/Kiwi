package snownee.kiwi;

import java.util.Objects;
import java.util.function.Supplier;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
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

	public boolean is(Object value) {
		return Objects.equals(this.value, value);
	}

	public boolean is(ItemStack stack) {
		return stack.is((Item) value);
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

}