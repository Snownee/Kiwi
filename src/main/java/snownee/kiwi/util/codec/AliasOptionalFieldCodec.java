package snownee.kiwi.util.codec;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

public class AliasOptionalFieldCodec<A> extends MapCodec<Optional<A>> {
	private final String name;
	private final Codec<A> elementCodec;
	private final boolean lenient;
	private final String alias;

	public AliasOptionalFieldCodec(final String name, final Codec<A> elementCodec, final boolean lenient, String alias) {
		this.name = name;
		this.elementCodec = elementCodec;
		this.lenient = lenient;
		this.alias = alias;
	}

	public static <A> MapCodec<A> defaulted(String name, Codec<A> elementCodec, A defaultValue, String alias) {
		return new AliasOptionalFieldCodec<>(name, elementCodec, false, alias).xmap(
				o -> o.orElse(defaultValue),
				a -> Objects.equals(a, defaultValue) ? Optional.empty() : Optional.of(a));
	}

	@Override
	public <T> DataResult<Optional<A>> decode(final DynamicOps<T> ops, final MapLike<T> input) {
		return input.get(name) != null ? decodeAs(ops, input, name) : decodeAs(ops, input, alias);
	}

	public <T> DataResult<Optional<A>> decodeAs(final DynamicOps<T> ops, final MapLike<T> input, String name) {
		final T value = input.get(name);
		if (value == null) {
			return DataResult.success(Optional.empty());
		}
		final DataResult<A> parsed = elementCodec.parse(ops, value);
		if (parsed.isError() && lenient) {
			return DataResult.success(Optional.empty());
		}
		return parsed.map(Optional::of).setPartial(parsed.resultOrPartial());
	}

	@Override
	public <T> RecordBuilder<T> encode(final Optional<A> input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
		if (input.isPresent()) {
			return prefix.add(name, elementCodec.encodeStart(ops, input.get()));
		}
		return prefix;
	}

	@Override
	public <T> Stream<T> keys(final DynamicOps<T> ops) {
		return Stream.of(ops.createString(name));
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final AliasOptionalFieldCodec<?> that = (AliasOptionalFieldCodec<?>) o;
		return lenient == that.lenient && name.equals(that.name) && elementCodec.equals(that.elementCodec) && alias.equals(that.alias);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, elementCodec, lenient, alias);
	}

	@Override
	public String toString() {
		return "AliasOptionalFieldCodec[%s(%s): %s]".formatted(name, alias, elementCodec);
	}
}