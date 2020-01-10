package snownee.kiwi;

import java.lang.reflect.Field;

import javax.annotation.Nullable;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class NamedEntry<T extends IForgeRegistryEntry<T>> {
    public final ResourceLocation name;
    public final T entry;
    @Nullable
    public final Field field;

    public NamedEntry(ResourceLocation name, T entry) {
        this(name, entry, null);
    }

    public NamedEntry(ResourceLocation name, T entry, Field field) {
        this.name = name;
        this.entry = entry;
        this.field = field;
    }
}
