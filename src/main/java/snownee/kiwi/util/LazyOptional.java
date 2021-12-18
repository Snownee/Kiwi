package snownee.kiwi.util;

import java.util.Objects;
import java.util.function.Supplier;

public class LazyOptional<T> implements Supplier<T> {

	private Supplier<T> supplier;
	private T value;

	private LazyOptional(Supplier<T> supplier) {
		this.supplier = supplier;
	}

	@Override
	public T get() {
		if (value == null) {
			value = supplier.get();
			supplier = null;
			Objects.requireNonNull(value);
		}
		return value;
	}

	public T orElse(T object) {
		return get();
	}

	public static <T> LazyOptional<T> of(Supplier<T> supplier) {
		Objects.requireNonNull(supplier);
		return new LazyOptional<>(supplier);
	}

}
