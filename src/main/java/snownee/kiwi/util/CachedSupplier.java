package snownee.kiwi.util;

import java.util.Objects;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

public class CachedSupplier<T> implements Supplier<T> {
	private final Object lock = new Object();
	private @Nullable Supplier<@Nullable T> delegate;
	private @Nullable T value;
	private @Nullable T fallback;

	public CachedSupplier(Supplier<@Nullable T> delegate) {
		this.delegate = delegate;
	}

	public CachedSupplier(Supplier<@Nullable T> delegate, @Nullable T fallback) {
		this.delegate = delegate;
		this.fallback = fallback;
	}

	@Nullable
	@Override
	public T get() {
		if (value != null) {
			return value;
		}
		synchronized (lock) {
			if (value == null) {
				value = Objects.requireNonNull(delegate).get();
				delegate = null;
			}
		}
		return value != null ? value : fallback;
	}
}
