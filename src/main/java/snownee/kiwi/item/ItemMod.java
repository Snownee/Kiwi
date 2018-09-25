package snownee.kiwi.item;

import net.minecraft.item.Item;

public class ItemMod extends Item implements IModItem
{
    private final String name;

    public ItemMod(String name)
    {
        super();
        this.name = name;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void register(String modid)
    {
        setRegistryName(modid, getName());
        setTranslationKey(modid + "." + getName());
    }

    @Override
    public Item cast()
    {
        return this;
    }
}
