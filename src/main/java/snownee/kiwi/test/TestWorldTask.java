package snownee.kiwi.test;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import snownee.kiwi.Kiwi;
import snownee.kiwi.schedule.impl.SimpleWorldTask;
import snownee.kiwi.schedule.impl.WorldTicker;

public class TestWorldTask extends SimpleWorldTask {
    public static final ResourceLocation ID = new ResourceLocation(Kiwi.MODID, "test");

    public TestWorldTask() {}

    public TestWorldTask(World world, TickEvent.Phase phase) {
        super(world, phase, null);
    }

    @Override
    public boolean tick(WorldTicker ticker) {
        if (++tick % 20 == 0) {
            MinecraftServer server = ticker.getWorld().getServer();
            if (server != null) {
                server.getPlayerList().sendMessage(new StringTextComponent("" + tick / 20));
            }
        }
        return tick >= 200;
    }
}
