package snownee.kiwi;

import java.lang.reflect.Field;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;

public class KiwiGOHolder<T> {
	public final T value;
	public final ResourceKey<T> key;
	@Nullable
	public final Field field;
	@Nullable
	public GroupSetting groupSetting;

	public KiwiGOHolder(T value, ResourceKey<T> key, @Nullable Field field) {
		this.value = value;
		this.key = key;
		this.field = field;
	}

	public void register() {
		//noinspection unchecked
		Registry<T> registry = (Registry<T>) Objects.requireNonNull(BuiltInRegistries.REGISTRY.get(key.registry()));
		Registry.register(registry, key, value);
	}
}
