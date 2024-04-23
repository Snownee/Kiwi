package snownee.kiwi.customization.block.loader;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import net.minecraft.world.level.block.state.BlockBehaviour;

public record InjectedBlockPropertiesCodec(Codec<BlockBehaviour.Properties> delegate) implements Codec<BlockBehaviour.Properties> {
	public static final ThreadLocal<BlockBehaviour.Properties> INJECTED = new ThreadLocal<>();

	@Override
	public <T> DataResult<Pair<BlockBehaviour.Properties, T>> decode(DynamicOps<T> ops, T input) {
		DataResult<Pair<BlockBehaviour.Properties, T>> result = delegate.decode(ops, input);
		BlockBehaviour.Properties properties = INJECTED.get();
		if (properties != null) {
			INJECTED.remove();
			return result.map(pair -> Pair.of(properties, pair.getSecond()));
		}
		return result;
	}

	@Override
	public <T> DataResult<T> encode(BlockBehaviour.Properties input, DynamicOps<T> ops, T prefix) {
		return delegate.encode(input, ops, prefix);
	}
}
