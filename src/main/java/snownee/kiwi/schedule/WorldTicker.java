package snownee.kiwi.schedule;

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import snownee.kiwi.util.MutablePair;

public class WorldTicker implements ITicker {
    private static final Map<DimensionType, MutablePair<WorldTicker>> tickers = Maps.newHashMap();
    @Nullable
    private World world;

    public static WorldTicker get(World world, TickEvent.Phase phase) {
        return get(world.dimension.getType(), phase);
    }

    public static WorldTicker get(DimensionType dimensionType, TickEvent.Phase phase) {
        MutablePair<WorldTicker> pair = tickers.get(dimensionType);
        if (pair == null) {
            pair = new MutablePair();
            tickers.put(dimensionType, pair);
        }
        WorldTicker ticker = pair.get(phase.ordinal());
        if (ticker == null) {
            ticker = new WorldTicker();
            pair.set(phase.ordinal(), ticker);
        }
        return ticker;
    }

    static {
        MinecraftForge.EVENT_BUS.register(WorldTicker.class);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.WorldTickEvent event) {
        MutablePair<WorldTicker> pair = tickers.get(event.world.dimension.getType());
        if (pair == null) {
            return;
        }
        WorldTicker ticker = pair.get(event.phase.ordinal());
        if (ticker == null) {
            return;
        }
        ticker.world = event.world;
        Scheduler.tick(ticker);
    }

    @SubscribeEvent
    public static void unloadWorld(WorldEvent.Unload event) {
        MutablePair<WorldTicker> pair = tickers.get(event.getWorld().getWorld().dimension.getType());
        if (pair == null) {
            return;
        }
        if (pair.left != null) {
            pair.left.destroy();
        }
        if (pair.right != null) {
            pair.right.destroy();
        }
    }

    private WorldTicker() {}

    @Nullable
    public World getWorld() {
        return world;
    }

    @Override
    public void destroy() {
        world = null;
    }
}
