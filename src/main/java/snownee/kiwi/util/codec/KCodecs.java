package snownee.kiwi.util.codec;

import java.util.List;

import com.mojang.serialization.Codec;

public final class KCodecs {
	public static <T> Codec<List<T>> compactList(Codec<T> elementCodec) {
		return Codec.withAlternative(elementCodec.listOf(), elementCodec, List::of);
	}
}
