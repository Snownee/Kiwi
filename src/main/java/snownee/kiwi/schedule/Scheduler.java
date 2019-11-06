package snownee.kiwi.schedule;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import snownee.kiwi.Kiwi;

@EventBusSubscriber
public final class Scheduler extends WorldSavedData {
    public static final String ID = Kiwi.MODID + "-schedule";
    public static final Scheduler INSTANCE = new Scheduler();

    private static final Map<ResourceLocation, Class<Task>> idToTask = Maps.newHashMap();
    private static final Map<Class<Task>, ResourceLocation> taskToId = Maps.newHashMap();

    protected static final Multimap<ITicker, Task> taskMap = LinkedListMultimap.create();

    protected static final Set<Task> serializableTasks = Sets.newHashSet();

    private Scheduler() {
        super(ID);
    }

    public static void register(ResourceLocation id, Class<Task> clazz) {
        if (idToTask.containsKey(id)) {
            Kiwi.logger.error("Duplicate task id: " + id);
        } else if (taskToId.containsKey(clazz)) {
            Kiwi.logger.error("Duplicate task class: " + clazz);
        } else if (!INBTSerializable.class.isAssignableFrom(clazz)) {
            Kiwi.logger.error("task " + id + " should implement INBTSerializable");
        } else {
            idToTask.put(id, clazz);
            taskToId.put(clazz, id);
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
        try {
            ResourceLocation type = taskToId.get(task.getClass());
            if (type != null) {
                CompoundNBT data = ((INBTSerializable<CompoundNBT>) task).serializeNBT();
                data.putString("type", type.toString());
                return data;
            }
        } catch (Exception e) {
            Kiwi.logger.catching(e);
        }
        return null;
    }

    public static void add(Task<?> task) {
        taskMap.put(task.ticker(), task);
    }

    public static <T extends ITicker> void tick(T ticker) {
        Iterator<Task> itr = taskMap.get(ticker).iterator();
        while (itr.hasNext()) {
            Task<T> task = itr.next();
            if (task.tick(ticker)) {
                itr.remove();
                // TODO: remove ticker if tasks are empty?
                serializableTasks.remove(task);
            }
        }
    }

    public void unload() {

    }

    public void load() {}

    @Override
    public boolean isDirty() {
        System.out.println(1);
        return !taskMap.isEmpty();
    }

    @Override
    public void read(CompoundNBT nbt) {
        serializableTasks.clear();
        ListNBT list = nbt.getList("tasks", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            Task task = deserialize(list.getCompound(i));
            if (task != null) {
                serializableTasks.add(task);
            }
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT data) {
        Set<Task> allTasks = Sets.newHashSet(serializableTasks);
        allTasks.addAll(taskMap.values());
        ListNBT list = new ListNBT();
        for (Task task : allTasks) {
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
}
