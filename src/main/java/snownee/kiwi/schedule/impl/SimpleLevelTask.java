package snownee.kiwi.schedule.impl;

import java.util.function.IntPredicate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import snownee.kiwi.Kiwi;
import snownee.kiwi.schedule.Task;

public class SimpleLevelTask extends Task<LevelTicker> implements INBTSerializable<CompoundTag> {

	protected int tick = 0;
	protected ResourceKey<Level> dimension;
	protected TickEvent.Phase phase;
	protected IntPredicate function;

	public SimpleLevelTask() {
	}

	public SimpleLevelTask(Level world, TickEvent.Phase phase, IntPredicate function) {
		this(world.dimension(), phase, function);
	}

	public SimpleLevelTask(ResourceKey<Level> dimensionType, TickEvent.Phase phase, IntPredicate function) {
		dimension = dimensionType;
		this.phase = phase;
		this.function = function;
	}

	@Override
	public boolean tick(LevelTicker ticker) {
		return function.test(++tick);
	}

	@Override
	public LevelTicker ticker() {
		return LevelTicker.get(dimension, phase);
	}

	@Override
	public boolean shouldSave() {
		return getClass() != SimpleLevelTask.class;
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag data = new CompoundTag();
		data.putInt("tick", tick);
		Level.RESOURCE_KEY_CODEC.encodeStart(NbtOps.INSTANCE, dimension).resultOrPartial(Kiwi.LOGGER::error).ifPresent(nbt -> {
			data.put("world", nbt);
		});
		data.putBoolean("start", phase == Phase.START);
		return data;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		dimension = Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, nbt.get("world")).resultOrPartial(Kiwi.LOGGER::error).orElse(Level.OVERWORLD);
		tick = nbt.getInt("tick");
		phase = nbt.getBoolean("start") ? Phase.START : Phase.END;
	}

}
