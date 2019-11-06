package snownee.kiwi.schedule;

import com.google.common.base.Function;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import snownee.kiwi.util.Util;

public class SimpleWorldTask extends Task<WorldTicker> implements INBTSerializable<CompoundNBT> {

    protected int tick = 0;
    protected DimensionType dimensionType;
    protected TickEvent.Phase phase;
    protected Function<Integer, Boolean> function;

    public SimpleWorldTask() {}

    public SimpleWorldTask(World world, TickEvent.Phase phase, Function<Integer, Boolean> function) {
        this(world.dimension.getType(), phase, function);
    }

    public SimpleWorldTask(DimensionType dimensionType, TickEvent.Phase phase, Function<Integer, Boolean> function) {
        this.dimensionType = dimensionType;
        this.phase = phase;
        this.function = function;
    }

    @Override
    public boolean tick(WorldTicker ticker) {
        return function.apply(++tick);
    }

    @Override
    public WorldTicker ticker() {
        return WorldTicker.get(dimensionType, phase);
    }

    @Override
    public boolean shouldSave() {
        return getClass() != SimpleWorldTask.class;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT data = new CompoundNBT();
        data.putInt("tick", tick);
        data.putString("world", Util.trimRL(dimensionType.getRegistryName()));
        data.putBoolean("start", phase == Phase.START);
        return data;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        ResourceLocation rl = Util.RL(nbt.getString("world"));
        dimensionType = DimensionType.byName(rl);
        if (dimensionType == null) {
            throw new NullPointerException("Task cannot find dimension " + rl);
        }
        tick = nbt.getInt("tick");
        phase = nbt.getBoolean("start") ? Phase.START : Phase.END;
    }

}
