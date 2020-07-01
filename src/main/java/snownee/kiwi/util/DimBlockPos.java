package snownee.kiwi.util;

import com.google.common.base.MoreObjects;

import net.minecraft.dispenser.IPosition;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import snownee.kiwi.Kiwi;

public class DimBlockPos extends BlockPos {
    private RegistryKey<World> dim;

    public DimBlockPos(RegistryKey<World> dim, int x, int y, int z) {
        super(x, y, z);
        this.dim = dim;
    }

    public DimBlockPos(RegistryKey<World> dim, double x, double y, double z) {
        super(x, y, z);
        this.dim = dim;
    }

    public DimBlockPos(RegistryKey<World> dim, Vector3d vec) {
        super(vec);
        this.dim = dim;
    }

    public DimBlockPos(RegistryKey<World> dim, IPosition source) {
        super(source);
        this.dim = dim;
    }

    public DimBlockPos(RegistryKey<World> dim, Vector3i source) {
        super(source);
        this.dim = dim;
    }

    public DimBlockPos(World world, Vector3i source) {
        this(world./*getDimension*/func_234923_W_(), source);
    }

    public RegistryKey<World> getDimension() {
        return dim;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        } else if (!(that instanceof DimBlockPos)) {
            return false;
        } else {
            return this.getDimension() == ((DimBlockPos) that).getDimension() && super.equals(that);
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 31 + getDimension().hashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("dim", dim).add("x", this.getX()).add("y", this.getY()).add("z", this.getZ()).toString();
    }

    public static CompoundNBT write(DimBlockPos pos) {
        CompoundNBT data = new CompoundNBT();
        World.field_234917_f_.encodeStart(NBTDynamicOps.INSTANCE, pos.dim).resultOrPartial(Kiwi.logger::error).ifPresent(nbt -> {
            data.put("Dim", nbt);
        });
        data.putInt("X", pos.getX());
        data.putInt("Y", pos.getY());
        data.putInt("Z", pos.getZ());
        return data;
    }

    public static DimBlockPos read(CompoundNBT tag) {
        RegistryKey<World> dimension = World.field_234917_f_.parse(NBTDynamicOps.INSTANCE, tag.get("Dim")).resultOrPartial(Kiwi.logger::error).orElse(World.field_234918_g_);
        return new DimBlockPos(dimension, tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z"));
    }
}
