package snownee.kiwi.handler;

import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class KEnergyStorage implements EnergyHandler, ValueIOSerializable {
	protected int energy;
	protected int capacity;
	protected int maxReceive;
	protected int maxExtract;

	public KEnergyStorage(int capacity) {
		this(capacity, capacity, capacity, 0);
	}

	public KEnergyStorage(int capacity, int maxTransfer) {
		this(capacity, maxTransfer, maxTransfer, 0);
	}

	public KEnergyStorage(int capacity, int maxReceive, int maxExtract) {
		this(capacity, maxReceive, maxExtract, 0);
	}

	public KEnergyStorage(int capacity, int maxReceive, int maxExtract, int energy) {
		this.capacity = capacity;
		this.maxReceive = maxReceive;
		this.maxExtract = maxExtract;
		this.energy = Mth.clamp(energy, 0, capacity);
	}

	public void setEnergy(int energy) {
		int old = this.energy;
		this.energy = Mth.clamp(energy, 0, getCapacityAsInt());
		if (old != this.energy) {
			onEnergyChanged();
		}
	}

	protected void onEnergyChanged() {
	}

	@Override
	public long getAmountAsLong() {
		return energy;
	}

	@Override
	public long getCapacityAsLong() {
		return capacity;
	}

	@Override
	public int insert(int amount, TransactionContext transaction) {
		if (maxExtract <= 0) {
			return 0;
		}

		int inserted = Mth.clamp(amount, 0, this.maxReceive);
		if (inserted > 0) {
			energy += inserted;
			onEnergyChanged();
		}
		return inserted;
	}

	@Override
	public int extract(int amount, TransactionContext transaction) {
		if (maxReceive <= 0) {
			return 0;
		}
		int extracted = Mth.clamp(amount, 0, this.maxExtract);
		if (extracted > 0) {
			energy -= extracted;
			onEnergyChanged();
		}
		return extracted;
	}

	@Override
	public void serialize(ValueOutput output) {
		output.putInt("energy", getAmountAsInt());
	}

	@Override
	public void deserialize(ValueInput input) {
		setEnergy(input.getIntOr("energy", 0));
	}
}
