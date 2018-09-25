package snownee.kiwi.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockMod extends Block implements IModBlock
{
    private final String name;

    public BlockMod(String name, Material materialIn)
    {
        super(materialIn);
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
    public Block cast()
    {
        return this;
    }
}
