package snownee.kiwi.schedule;

import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import snownee.kiwi.Kiwi;

/**
 * @since 2.4
 */
@EventBusSubscriber
public final class Scheduler extends WorldSavedData {
	public static final String ID = Kiwi.MODID + "-schedule";
	public static final Scheduler INSTANCE = new Scheduler();

	private static final Map<ResourceLocation, Class<Task>> idToTask = Maps.newHashMap();
	private static final Map<Class<Task>, ResourceLocation> taskToId = Maps.newHashMap();

	protected static final Multimap<ITicker, Task> taskMap = LinkedListMultimap.create();

	private Scheduler() {
		super(ID);
	}

	public static void register(ResourceLocation id, Class<? extends Task> clazz) {
		if (idToTask.containsKey(id)) {
			Kiwi.logger.error("Duplicate task id: " + id);
		} else if (taskToId.containsKey(clazz)) {
			Kiwi.logger.error("Duplicate task class: " + clazz);
		} else if (!INBTSerializable.class.isAssignableFrom(clazz)) {
			Kiwi.logger.error("task " + id + " should implement INBTSerializable");
		} else {
			idToTask.put(id, (Class<Task>) clazz);
			taskToId.put((Class<Task>) clazz, id);
		}
	}

	public Task deserialize(CompoundNBT data) {
		try {
			ResourceLocation type = new ResourceLocation(data.getString("type"));
			Class<Task> clazz = idToTask.get(type);
			if (clazz != null) {
				Task task = clazz.newInstance();
				((INBTSerializable<CompoundNBT>) task).deserializeNBT(data);
				return task;
			}
		} catch (Exception e) {
			Kiwi.logger.catching(e);
		}
		return null;
	}

	public CompoundNBT serialize(Task task) {
		if (task.shouldSave()) {
			try {
				ResourceLocation type = taskToId.get(task.getClass());
				CompoundNBT data = ((INBTSerializable<CompoundNBT>) task).serializeNBT();
				data.putString("type", type.toString());
				return data;
			} catch (Exception e) {
				Kiwi.logger.catching(e);
			}
		}
		return null;
	}

	public static void add(Task<?> task) {
		ITicker ticker = task.ticker();
		if (ticker != null) {
			taskMap.put(ticker, task);
		}
	}

	public static void remove(Task<?> task) {
		taskMap.values().remove(task);
	}

	public static <T extends ITicker> void tick(T ticker) {
		Iterator<Task> itr = taskMap.get(ticker).iterator();
		while (itr.hasNext()) {
			Task<T> task = itr.next();
			if (task.tick(ticker)) {
				itr.remove();
			}
		}
	}

	@Override
	public boolean isDirty() {
		return !taskMap.isEmpty();
	}

	@Override
	public void load(CompoundNBT nbt) {
		ListNBT list = nbt.getList("tasks", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			Task task = deserialize(list.getCompound(i));
			if (task != null) {
				add(task);
			}
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT data) {
		ListNBT list = new ListNBT();
		for (Task task : taskMap.values()) {
			CompoundNBT nbt = serialize(task);
			if (nbt != null) {
				list.add(nbt);
			}
		}
		if (!list.isEmpty()) {
			data.put("tasks", list);
		}
		return data;
	}

	public static void clear() {
		taskMap.keySet().forEach(ITicker::destroy);
		taskMap.clear();
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public static void clientLoggedOut(ClientPlayerNetworkEvent.LoggedOutEvent event) {
		clear();
	}

	@SubscribeEvent
	public static void serverStopped(FMLServerStoppedEvent event) {
		clear();
	}
}
