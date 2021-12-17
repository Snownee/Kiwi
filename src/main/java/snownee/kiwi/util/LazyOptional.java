package snownee.kiwi.util;

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
			if (value == null) {
				throw new NullPointerException();
			}
		}
		return value;
	}

	public T orElse(T object) {
		return get();
	}

	public static <T> LazyOptional<T> of(Supplier<T> supplier) {
		return new LazyOptional<>(supplier);
	}

}
