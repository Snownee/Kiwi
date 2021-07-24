package snownee.kiwi.schedule.impl;

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import snownee.kiwi.schedule.ITicker;
import snownee.kiwi.schedule.Scheduler;
import snownee.kiwi.util.MutablePair;

public class LevelTicker implements ITicker {
	private static final Map<ResourceKey<Level>, MutablePair<LevelTicker>> tickers = Maps.newHashMap();

	public static LevelTicker get(Level level, TickEvent.Phase phase) {
		return get(level.dimension(), phase);
	}

	public static LevelTicker get(ResourceKey<Level> dimension, TickEvent.Phase phase) {
		MutablePair<LevelTicker> pair = tickers.get(dimension);
		if (pair == null) {
			pair = new MutablePair<>();
			tickers.put(dimension, pair);
		}
		LevelTicker ticker = pair.get(phase.ordinal());
		if (ticker == null) {
			ticker = new LevelTicker(dimension);
			pair.set(phase.ordinal(), ticker);
		}
		return ticker;
	}

	static {
		MinecraftForge.EVENT_BUS.register(LevelTicker.class);
	}

	@SubscribeEvent
	public static void onTick(TickEvent.WorldTickEvent event) {
		MutablePair<LevelTicker> pair = tickers.get(event.world.dimension());
		if (pair == null) {
			return;
		}
		LevelTicker ticker = pair.get(event.phase.ordinal());
		if (ticker == null) {
			return;
		}
		ticker.level = event.world;
		Scheduler.tick(ticker);
	}

	@SubscribeEvent
	public static void unloadLevel(WorldEvent.Unload event) {
		if (!(event.getWorld() instanceof Level)) {
			return;
		}
		MutablePair<LevelTicker> pair = tickers.get(((Level) event.getWorld()).dimension());
		if (pair == null) {
			return;
		}
		if (pair.left != null) {
			pair.left.level = null;
		}
		if (pair.right != null) {
			pair.right.level = null;
		}
	}

	@Nullable
	private Level level;
	private final ResourceKey<Level> dimension;

	private LevelTicker(ResourceKey<Level> dimension) {
		this.dimension = dimension;
	}

	@Nullable
	public Level getLevel() {
		return level;
	}

	@Override
	public void destroy() {
		MutablePair<LevelTicker> pair = tickers.get(dimension);
		if (pair != null) {
			if (pair.left == this) {
				pair.left = null;
			}
			if (pair.right == this) {
				pair.left = null;
			}
		}
		level = null;
	}
}
