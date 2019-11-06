package snownee.kiwi.schedule.impl;

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import snownee.kiwi.schedule.ITicker;
import snownee.kiwi.schedule.Scheduler;
import snownee.kiwi.util.MutablePair;

public class WorldTicker implements ITicker {
    private static final Map<DimensionType, MutablePair<WorldTicker>> tickers = Maps.newHashMap();

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
            ticker = new WorldTicker(dimensionType);
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
            pair.left.world = null;
        }
        if (pair.right != null) {
            pair.right.world = null;
        }
    }

    @Nullable
    private World world;
    private final DimensionType dimensionType;

    private WorldTicker(DimensionType dimensionType) {
        this.dimensionType = dimensionType;
    }

    @Nullable
    public World getWorld() {
        return world;
    }

    @Override
    public void destroy() {
        MutablePair<WorldTicker> pair = tickers.get(dimensionType);
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
