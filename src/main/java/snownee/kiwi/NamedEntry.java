package snownee.kiwi;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class NamedEntry
{
    final ResourceLocation name;
    final IForgeRegistryEntry<?> entry;

    public NamedEntry(ResourceLocation name, IForgeRegistryEntry<?> entry)
    {
        this.name = name;
        this.entry = entry;
    }
}
