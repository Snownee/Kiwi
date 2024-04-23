package snownee.kiwi.util;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

public class CachedSupplier<T> implements Supplier<T> {
	private final Object lock = new Object();
	private Supplier<T> delegate;
	@Nullable
	private T value;
	@Nullable
	private T fallback;

	public CachedSupplier(Supplier<T> delegate) {
		this.delegate = delegate;
	}

	public CachedSupplier(Supplier<T> delegate, @Nullable T fallback) {
		this.delegate = delegate;
		this.fallback = fallback;
	}

	@Override
	public T get() {
		if (value != null) {
			return value;
		}
		synchronized (lock) {
			if (value == null) {
				value = delegate.get();
			}
			if (value != null) {
				delegate = null;
			}
		}
		return value != null ? value : fallback;
	}
}