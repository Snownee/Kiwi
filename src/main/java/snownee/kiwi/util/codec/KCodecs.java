package snownee.kiwi.util.codec;

import java.util.List;

import com.mojang.serialization.Codec;

import net.minecraft.util.ExtraCodecs;

public final class KCodecs {
	public static <T> Codec<List<T>> compactList(Codec<T> elementCodec) {
		return ExtraCodecs.withAlternative(elementCodec.listOf(), elementCodec, List::of);
	}
}
