package snownee.kiwi.schedule.impl;

import com.google.common.base.Function;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.Type;
import snownee.kiwi.schedule.Task;

public class SimpleGlobalTask extends Task<GlobalTicker> implements INBTSerializable<CompoundNBT> {

    protected int tick = 0;
    protected Type side;
    protected Phase phase;
    protected Function<Integer, Boolean> function;

    public SimpleGlobalTask() {}

    public SimpleGlobalTask(Type side, Phase phase, Function<Integer, Boolean> function) {
        this.side = side;
        this.phase = phase;
        this.function = function;
    }

    @Override
    public boolean tick(GlobalTicker ticker) {
        return function.apply(++tick);
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
        data.putBoolean("server", side == Type.SERVER);
        data.putBoolean("start", phase == Phase.START);
        return data;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        tick = nbt.getInt("tick");
        side = nbt.getBoolean("server") ? Type.SERVER : Type.CLIENT;
        phase = nbt.getBoolean("start") ? Phase.START : Phase.END;
    }

}
