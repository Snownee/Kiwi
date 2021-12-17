package snownee.kiwi;

import java.lang.reflect.Field;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public class NamedEntry<T> {
	public final ResourceLocation name;
	public final T entry;
	public final Registry<T> registry;
	@Nullable
	public final Field field;

	public NamedEntry(ResourceLocation name, T entry, Registry<T> registry, @Nullable Field field) {
		this.name = name;
		this.entry = entry;
		this.field = field;
		this.registry = registry;
	}
}
