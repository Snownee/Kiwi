package snownee.kiwi.util.codec;

import java.util.function.Function;

import com.mojang.serialization.DataResult;

public final class KCodecs {
	private static final Function<Object, Object> UNSUPPORTED_GETTER = $ -> {
		throw new UnsupportedOperationException("Serialization is not supported for this field");
	};

	private KCodecs() {
	}

	@SuppressWarnings("unchecked")
	public static <A, B> Function<A, B> unsupportedGetter() {
		return (Function<A, B>) UNSUPPORTED_GETTER;
	}

	public static <T> DataResult<T> tryCatch(ThrowingSupplier<T> supplier) {
		try {
			return DataResult.success(supplier.get());
		} catch (Exception e) {
			return DataResult.error(e::getMessage);
		}
	}
}
