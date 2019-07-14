package snownee.kiwi;

import net.minecraftforge.registries.IForgeRegistryEntry;

public class NamedEntry
{
    final String name;
    final IForgeRegistryEntry<?> entry;

    public NamedEntry(String name, IForgeRegistryEntry<?> entry)
    {
        this.name = name;
        this.entry = entry;
    }
}
