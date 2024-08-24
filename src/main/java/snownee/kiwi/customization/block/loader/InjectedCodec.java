package snownee.kiwi.customization.block.loader;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

public record InjectedCodec<I>(Codec<I> delegate, ThreadLocal<I> threadLocal) implements Codec<I> {
	@Override
	public <T> DataResult<Pair<I, T>> decode(DynamicOps<T> ops, T input) {
		DataResult<Pair<I, T>> result = delegate.decode(ops, input);
		I properties = threadLocal.get();
		if (properties != null) {
			threadLocal.remove();
			return result.map(pair -> Pair.of(properties, pair.getSecond()));
		}
		return result;
	}

	@Override
	public <T> DataResult<T> encode(I input, DynamicOps<T> ops, T prefix) {
		return delegate.encode(input, ops, prefix);
	}
}
