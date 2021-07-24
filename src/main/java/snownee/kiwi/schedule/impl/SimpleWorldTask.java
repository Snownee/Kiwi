package snownee.kiwi.schedule.impl;

import java.util.function.IntPredicate;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import snownee.kiwi.Kiwi;
import snownee.kiwi.schedule.Task;

public class SimpleWorldTask extends Task<WorldTicker> implements INBTSerializable<CompoundNBT> {

	protected int tick = 0;
	protected RegistryKey<World> dimension;
	protected TickEvent.Phase phase;
	protected IntPredicate function;

	public SimpleWorldTask() {
	}

	public SimpleWorldTask(World world, TickEvent.Phase phase, IntPredicate function) {
		this(world.dimension(), phase, function);
	}

	public SimpleWorldTask(RegistryKey<World> dimensionType, TickEvent.Phase phase, IntPredicate function) {
		dimension = dimensionType;
		this.phase = phase;
		this.function = function;
	}

	@Override
	public boolean tick(WorldTicker ticker) {
		return function.test(++tick);
	}

	@Override
	public WorldTicker ticker() {
		return WorldTicker.get(dimension, phase);
	}

	@Override
	public boolean shouldSave() {
		return getClass() != SimpleWorldTask.class;
	}

	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT data = new CompoundNBT();
		data.putInt("tick", tick);
		World.RESOURCE_KEY_CODEC.encodeStart(NBTDynamicOps.INSTANCE, dimension).resultOrPartial(Kiwi.logger::error).ifPresent(nbt -> {
			data.put("world", nbt);
		});
		data.putBoolean("start", phase == Phase.START);
		return data;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		dimension = World.RESOURCE_KEY_CODEC.parse(NBTDynamicOps.INSTANCE, nbt.get("world")).resultOrPartial(Kiwi.logger::error).orElse(World.OVERWORLD);
		tick = nbt.getInt("tick");
		phase = nbt.getBoolean("start") ? Phase.START : Phase.END;
	}

}
