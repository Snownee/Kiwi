package snownee.kiwi.schedule.impl;

import java.util.function.IntPredicate;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.fml.LogicalSide;
import snownee.kiwi.schedule.Task;

public class SimpleGlobalTask extends Task<GlobalTicker> implements INBTSerializable<CompoundNBT> {

	protected int tick = 0;
	protected LogicalSide side;
	protected Phase phase;
	protected IntPredicate function;

	public SimpleGlobalTask() {
	}

	public SimpleGlobalTask(LogicalSide side, Phase phase, IntPredicate function) {
		this.side = side;
		this.phase = phase;
		this.function = function;
	}

	@Override
	public boolean tick(GlobalTicker ticker) {
		return function.test(++tick);
	}

	@Override
	public GlobalTicker ticker() {
		return GlobalTicker.get(side, phase);
	}

	@Override
	public boolean shouldSave() {
		return getClass() != SimpleGlobalTask.class;
	}

	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT data = new CompoundNBT();
		data.putInt("tick", tick);
		data.putBoolean("client", side.isClient());
		data.putBoolean("start", phase == Phase.START);
		return data;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		tick = nbt.getInt("tick");
		side = nbt.getBoolean("client") ? LogicalSide.CLIENT : LogicalSide.SERVER;
		phase = nbt.getBoolean("start") ? Phase.START : Phase.END;
	}

}
