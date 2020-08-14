package snownee.kiwi.schedule.impl;

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import snownee.kiwi.schedule.ITicker;
import snownee.kiwi.schedule.Scheduler;
import snownee.kiwi.util.MutablePair;

public class WorldTicker implements ITicker {
    private static final Map<RegistryKey<World>, MutablePair<WorldTicker>> tickers = Maps.newHashMap();

    public static WorldTicker get(World world, TickEvent.Phase phase) {
        return get(world./*getDimension*/func_234923_W_(), phase);
    }

    public static WorldTicker get(RegistryKey<World> dimension, TickEvent.Phase phase) {
        MutablePair<WorldTicker> pair = tickers.get(dimension);
        if (pair == null) {
            pair = new MutablePair();
            tickers.put(dimension, pair);
        }
        WorldTicker ticker = pair.get(phase.ordinal());
        if (ticker == null) {
            ticker = new WorldTicker(dimension);
            pair.set(phase.ordinal(), ticker);
        }
        return ticker;
    }

    static {
        MinecraftForge.EVENT_BUS.register(WorldTicker.class);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.WorldTickEvent event) {
        MutablePair<WorldTicker> pair = tickers.get(event.world./*getDimension*/func_234923_W_());
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
        if (!(event.getWorld() instanceof World)) {
            return;
        }
        MutablePair<WorldTicker> pair = tickers.get(((World) event.getWorld())./*getDimension*/func_234923_W_());
        if (pair == null) {
            return;
        }
        if (pair.left != null) {
            pair.left.world = null;
        }
        if (pair.right != null) {
            pair.right.world = null;
        }
    }

    @Nullable
    private World world;
    private final RegistryKey<World> dimension;

    private WorldTicker(RegistryKey<World> dimension) {
        this.dimension = dimension;
    }

    @Nullable
    public World getWorld() {
        return world;
    }

    @Override
    public void destroy() {
        MutablePair<WorldTicker> pair = tickers.get(dimension);
        if (pair != null) {
            if (pair.left == this) {
                pair.left = null;
            }
            if (pair.right == this) {
                pair.left = null;
            }
        }
        world = null;
    }
}
