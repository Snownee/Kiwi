package snownee.kiwi.test;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import snownee.kiwi.schedule.impl.SimpleWorldTask;
import snownee.kiwi.schedule.impl.WorldTicker;

public class MyTask extends SimpleWorldTask {
	public static final ResourceLocation ID = new ResourceLocation("my_mod", "test");
	private String words;

	public MyTask() {
	}

	public MyTask(World world, TickEvent.Phase phase, String words) {
		super(world, phase, null);
		this.words = words;
	}

	@Override
	public boolean tick(WorldTicker ticker) {
		if (++tick >= 50) {
			MinecraftServer server = ticker.getWorld().getServer();
			if (server != null) {
				TextComponent text = new StringTextComponent(words);
				server.getPlayerList()./*sendMessage*/func_232641_a_(text, ChatType.SYSTEM, Util.DUMMY_UUID);
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void deserializeNBT(CompoundNBT data) {
		super.deserializeNBT(data);
		words = data.getString("words");
	}

	@Override
	public CompoundNBT serializeNBT() {
		CompoundNBT data = super.serializeNBT();
		data.putString("words", words);
		return data;
	}
}
