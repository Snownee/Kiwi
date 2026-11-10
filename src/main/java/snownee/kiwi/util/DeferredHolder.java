package snownee.kiwi.util;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/**
 * A registry reference that is resolved lazily on first use.
 *
 * <p>The reference keeps its {@link ResourceKey} and looks the value up in
 * {@link BuiltInRegistries} when it is first accessed. The resolved value is then bound to this
 * holder, so subsequent accesses do not touch the registry again.
 *
 * @param <R> registry element type
 * @param <T> concrete element type
 */
public class DeferredHolder<R, T extends R> extends Holder.Reference<R> implements Supplier<T> {
	private static final HolderOwner<?> OWNER = new HolderOwner<>() {
	};

	public static <R, T extends R> Codec<DeferredHolder<R, T>> codec(ResourceKey<? extends Registry<R>> registryKey) {
		return ResourceKey.codec(registryKey).xmap(DeferredHolder::new, DeferredHolder::getKey);
	}

	public static <R, T extends R> DeferredHolder<R, T> create(
			ResourceKey<? extends Registry<R>> registryKey,
			Identifier valueName) {
		return create(ResourceKey.create(registryKey, valueName));
	}

	public static <R, T extends R> DeferredHolder<R, T> create(Identifier registryName, Identifier valueName) {
		return create(ResourceKey.createRegistryKey(registryName), valueName);
	}

	public static <R, T extends R> DeferredHolder<R, T> create(ResourceKey<R> key) {
		return new DeferredHolder<>(key);
	}

	@SuppressWarnings("unchecked")
	protected DeferredHolder(ResourceKey<R> key) {
		super(Type.STAND_ALONE, (HolderOwner<R>) OWNER, Objects.requireNonNull(key), null);
	}

	public ResourceKey<R> getKey() {
		return key();
	}

	public Identifier getId() {
		return key().identifier();
	}

	@SuppressWarnings("unchecked")
	public Optional<T> asOptional() {
		if (super.isBound()) {
			return Optional.of((T) super.value());
		}
		synchronized (this) {
			if (!super.isBound()) {
				R value = registry().getValue(key());
				if (value == null) {
					return Optional.empty();
				}
				super.bindValue(value);
			}
			return Optional.of((T) super.value());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public T value() {
		if (super.isBound()) {
			return (T) super.value();
		}
		synchronized (this) {
			if (!super.isBound()) {
				R value = registry().getValue(key());
				if (value == null) {
					throw new IllegalStateException(
							"Registry entry " + key().identifier() + " is missing from " + key().registryKey().identifier());
				}
				super.bindValue(value);
			}
			return (T) super.value();
		}
	}

	@Override
	public T get() {
		return value();
	}

	@Override
	public boolean isBound() {
		return super.isBound() || registry().containsKey(key());
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private Registry<R> registry() {
		ResourceKey<Registry<R>> registryKey = key().registryKey();
		Registry<?> registry = BuiltInRegistries.REGISTRY.getValue((ResourceKey) registryKey);
		if (registry == null) {
			throw new IllegalStateException("Unknown registry: " + registryKey.identifier());
		}
		return (Registry<R>) registry;
	}
}
