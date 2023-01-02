package snownee.kiwi;

import java.lang.reflect.Field;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;

public class NamedEntry<T> {
	public final ResourceLocation name;
	public final T entry;
	public final Object registry;
	@Nullable
	public final Field field;
	@Nullable
	public GroupSetting groupSetting;

	public NamedEntry(ResourceLocation name, T entry, Object registry, @Nullable Field field) {
		this.name = name;
		this.entry = entry;
		this.field = field;
		this.registry = registry;
	}
}
