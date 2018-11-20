package snownee.kiwi.handler;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.energy.EnergyStorage;

public class Battery extends EnergyStorage
{

    public Battery(int capacity)
    {
        super(capacity);
    }

    public Battery(int capacity, int maxTransfer)
    {
        super(capacity, maxTransfer, maxTransfer, 0);
    }

    public Battery(int capacity, int maxReceive, int maxExtract)
    {
        super(capacity, maxReceive, maxExtract, 0);
    }

    public Battery(int capacity, int maxReceive, int maxExtract, int energy)
    {
        super(capacity, maxReceive, maxExtract, energy);
    }

    public Battery readFromNBT(NBTTagCompound nbt)
    {
        if (nbt.hasKey("Energy", Constants.NBT.TAG_INT))
        {
            energy = nbt.getInteger("Energy");
        }
        else
        {
            energy = 0;
        }
        return this;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        if (energy > 0)
        {
            nbt.setInteger("Energy", energy);
        }
        return nbt;
    }

    public void setEnergy(int energy)
    {
        int old = this.energy;
        this.energy = MathHelper.clamp(energy, 0, getMaxEnergyStored());
        if (old != this.energy)
        {
            onEnergyChanged();
        }
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate)
    {
        int amount = super.extractEnergy(maxExtract, simulate);
        if (!simulate && amount > 0)
        {
            onEnergyChanged();
        }
        return amount;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate)
    {
        int amount = super.receiveEnergy(maxReceive, simulate);
        if (!simulate && amount > 0)
        {
            onEnergyChanged();
        }
        return amount;
    }

    protected void onEnergyChanged()
    {
    }

}
