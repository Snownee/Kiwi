package snownee.kiwi.test;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import snownee.kiwi.schedule.impl.LevelTicker;
import snownee.kiwi.schedule.impl.SimpleLevelTask;

public class MyTask extends SimpleLevelTask {
	public static final ResourceLocation ID = new ResourceLocation("my_mod", "test");
	private String words;

	public MyTask() {
	}

	public MyTask(Level world, TickEvent.Phase phase, String words) {
		super(world, phase, null);
		this.words = words;
	}

	@Override
	public boolean tick(LevelTicker ticker) {
		if (++tick >= 50) {
			MinecraftServer server = ticker.getLevel().getServer();
			if (server != null) {
				Component text = Component.literal(words);
				server.getPlayerList().broadcastSystemMessage(text, false);
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void deserializeNBT(CompoundTag data) {
		super.deserializeNBT(data);
		words = data.getString("words");
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag data = super.serializeNBT();
		data.putString("words", words);
		return data;
	}
}
