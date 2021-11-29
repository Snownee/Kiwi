package snownee.kiwi.handler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraftforge.energy.EnergyStorage;

public class Battery extends EnergyStorage {

	public Battery(int capacity) {
		super(capacity);
	}

	public Battery(int capacity, int maxTransfer) {
		super(capacity, maxTransfer, maxTransfer, 0);
	}

	public Battery(int capacity, int maxReceive, int maxExtract) {
		super(capacity, maxReceive, maxExtract, 0);
	}

	public Battery(int capacity, int maxReceive, int maxExtract, int energy) {
		super(capacity, maxReceive, maxExtract, energy);
	}

	public Battery readFromNBT(CompoundTag nbt) {
		if (nbt.contains("Energy", Tag.TAG_INT)) {
			energy = nbt.getInt("Energy");
		} else {
			energy = 0;
		}
		return this;
	}

	public CompoundTag writeToNBT(CompoundTag nbt) {
		if (energy > 0) {
			nbt.putInt("Energy", energy);
		}
		return nbt;
	}

	public void setEnergy(int energy) {
		int old = this.energy;
		this.energy = Mth.clamp(energy, 0, getMaxEnergyStored());
		if (old != this.energy) {
			onEnergyChanged();
		}
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {
		int amount = super.extractEnergy(maxExtract, simulate);
		if (!simulate && amount > 0) {
			onEnergyChanged();
		}
		return amount;
	}

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
		int amount = super.receiveEnergy(maxReceive, simulate);
		if (!simulate && amount > 0) {
			onEnergyChanged();
		}
		return amount;
	}

	protected void onEnergyChanged() {
	}

}
