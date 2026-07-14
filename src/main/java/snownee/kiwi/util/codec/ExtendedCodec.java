package snownee.kiwi.util.codec;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

public class ExtendedCodec<A, B> implements Codec<A> {
	private final Codec<A> delegate;
	private final Codec<B> extension;
	private final BiConsumer<A, B> extensionSetter;
	private final Function<A, @Nullable B> extensionGetter;

	public ExtendedCodec(Codec<A> delegate, Codec<B> extension, BiConsumer<A, B> setter, Function<A, @Nullable B> getter) {
		this.delegate = delegate;
		this.extension = extension;
		this.extensionSetter = setter;
		this.extensionGetter = getter;
	}

	@Override
	public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
		DataResult<Pair<A, T>> result = delegate.decode(ops, input);
		if (result.isError()) {
			return result;
		}
		A a = result.getOrThrow().getFirst();
		return extension.decode(ops, input).map(pair -> {
			B b = pair.getFirst();
			extensionSetter.accept(a, b);
			return Pair.of(a, pair.getSecond());
		});
	}

	@Override
	public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
		DataResult<T> result = delegate.encode(input, ops, prefix);
		if (result.isError()) {
			return result;
		}
		B b = extensionGetter.apply(input);
		if (b == null) {
			return result;
		}
		return extension.encode(b, ops, result.getOrThrow());
	}
}
