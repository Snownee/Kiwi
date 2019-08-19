package snownee.kiwi.util;

import com.google.common.base.MoreObjects;

import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class DimBlockPos extends BlockPos
{
    private int dim;

    public DimBlockPos(int dim, int x, int y, int z)
    {
        super(x, y, z);
        this.dim = dim;
    }

    public DimBlockPos(int dim, double x, double y, double z)
    {
        super(x, y, z);
        this.dim = dim;
    }

    public DimBlockPos(Entity source)
    {
        super(source);
        this.dim = source.world.getWorldType().getId();
    }

    public DimBlockPos(int dim, Vec3d vec)
    {
        super(vec);
        this.dim = dim;
    }

    public DimBlockPos(int dim, IPosition source)
    {
        super(source);
        this.dim = dim;
    }

    public DimBlockPos(int dim, Vec3i source)
    {
        super(source);
        this.dim = dim;
    }

    public DimBlockPos(World world, Vec3i source)
    {
        super(source);
        this.dim = world.getWorldType().getId();
    }

    public int getDimension()
    {
        return dim;
    }

    @Override
    public boolean equals(Object that)
    {
        if (this == that)
        {
            return true;
        }
        else if (!(that instanceof DimBlockPos))
        {
            return false;
        }
        else
        {
            return this.getDimension() == ((DimBlockPos) that).getDimension() && super.equals(that);
        }
    }

    @Override
    public int hashCode()
    {
        return super.hashCode() * 31 + getDimension() - 1; // Overworld id == 1
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this).add("dim", dim).add("x", this.getX()).add("y", this.getY()).add("z", this.getZ()).toString();
    }

    public static CompoundNBT write(DimBlockPos pos)
    {
        CompoundNBT compoundnbt = new CompoundNBT();
        compoundnbt.putInt("Dim", pos.getDimension());
        compoundnbt.putInt("X", pos.getX());
        compoundnbt.putInt("Y", pos.getY());
        compoundnbt.putInt("Z", pos.getZ());
        return compoundnbt;
    }

    public static DimBlockPos read(CompoundNBT tag)
    {
        return new DimBlockPos(tag.getInt("Dim"), tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z"));
    }
}
