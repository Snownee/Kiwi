package snownee.kiwi.schedule;

import java.util.Iterator;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fmlserverevents.FMLServerStoppedEvent;
import snownee.kiwi.Kiwi;

/**
 * @since 2.4
 */
@EventBusSubscriber
public final class Scheduler extends SavedData {
	public static final String ID = Kiwi.MODID + "-schedule";
	public static final Scheduler INSTANCE = new Scheduler();

	private static final BiMap<ResourceLocation, Class<Task>> idMap = HashBiMap.create();

	protected static final Multimap<ITicker, Task> taskMap = LinkedListMultimap.create();

	private Scheduler() {
	}

	public static void register(ResourceLocation id, Class<? extends Task> clazz) {
		if (idMap.containsKey(id)) {
			Kiwi.logger.error("Duplicate task id: " + id);
		} else if (idMap.containsValue(clazz)) {
			Kiwi.logger.error("Duplicate task class: " + clazz);
		} else if (!INBTSerializable.class.isAssignableFrom(clazz)) {
			Kiwi.logger.error("task " + id + " should implement INBTSerializable");
		} else {
			idMap.put(id, (Class<Task>) clazz);
		}
	}

	public static Task deserialize(CompoundTag data) {
		try {
			ResourceLocation type = new ResourceLocation(data.getString("type"));
			Class<Task> clazz = idMap.get(type);
			if (clazz != null) {
				Task task = clazz.getDeclaredConstructor().newInstance();
				((INBTSerializable<CompoundTag>) task).deserializeNBT(data);
				return task;
			}
		} catch (Exception e) {
			Kiwi.logger.catching(e);
		}
		return null;
	}

	public CompoundTag serialize(Task task) {
		if (task.shouldSave()) {
			try {
				ResourceLocation type = idMap.inverse().get(task.getClass());
				CompoundTag data = ((INBTSerializable<CompoundTag>) task).serializeNBT();
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

	public static Scheduler load(CompoundTag nbt) {
		ListTag list = nbt.getList("tasks", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			Task task = deserialize(list.getCompound(i));
			if (task != null) {
				add(task);
			}
		}
		return INSTANCE;
	}

	@Override
	public CompoundTag save(CompoundTag data) {
		ListTag list = new ListTag();
		for (Task task : taskMap.values()) {
			CompoundTag nbt = serialize(task);
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
