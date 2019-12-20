package snownee.kiwi;

import net.minecraftforge.registries.IForgeRegistryEntry;

public class NamedEntry<T extends IForgeRegistryEntry<T>>
{
    final String name;
    final T entry;

    public NamedEntry(String name, T entry)
    {
        this.name = name;
        this.entry = entry;
    }
}
