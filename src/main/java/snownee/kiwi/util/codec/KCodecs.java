package snownee.kiwi.util.codec;

import java.util.List;
import java.util.function.Function;

import com.mojang.serialization.Codec;

public final class KCodecs {
	private static final Function<Object, Object> UNSUPPORTED_GETTER = s -> {
		throw new UnsupportedOperationException("Serialization is not supported for this field");
	};

	public static <T> Codec<List<T>> compactList(Codec<T> elementCodec) {
		return Codec.withAlternative(elementCodec.listOf(), elementCodec, List::of);
	}

	public static <T, R> Function<T, R> unsupportedGetter() {
		//noinspection unchecked
		return (Function<T, R>) UNSUPPORTED_GETTER;
	}
}
