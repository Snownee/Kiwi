package snownee.kiwi.util.codec;

import java.util.function.Function;

public final class KCodecs {
	private KCodecs() {
	}

	public static <A, B> Function<A, B> unsupportedGetter() {
		return $ -> {
			throw new UnsupportedOperationException();
		};
	}
}
