package snownee.kiwi;

import java.lang.reflect.Field;

import javax.annotation.Nullable;

import net.minecraftforge.registries.IForgeRegistryEntry;

public class NamedEntry<T extends IForgeRegistryEntry<T>> {
    public final String name;
    public final T entry;
    @Nullable
    public final Field field;

    public NamedEntry(String name, T entry) {
        this(name, entry, null);
    }

    public NamedEntry(String name, T entry, Field field) {
        this.name = name;
        this.entry = entry;
        this.field = field;
    }
}
