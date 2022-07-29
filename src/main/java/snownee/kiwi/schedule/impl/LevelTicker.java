package snownee.kiwi.schedule.impl;

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import snownee.kiwi.schedule.ITicker;
import snownee.kiwi.schedule.Scheduler;

public class LevelTicker implements ITicker {
	private static final Map<ResourceKey<Level>, LevelTicker[]> tickers = Maps.newHashMap();

	public static LevelTicker get(Level level, TickEvent.Phase phase) {
		return get(level.dimension(), phase);
	}

	public static LevelTicker get(ResourceKey<Level> dimension, TickEvent.Phase phase) {
		LevelTicker[] pair = tickers.computeIfAbsent(dimension, $ -> new LevelTicker[2]);
		LevelTicker ticker = pair[phase.ordinal()];
		if (ticker == null) {
			ticker = new LevelTicker(dimension);
			pair[phase.ordinal()] = ticker;
		}
		return ticker;
	}

	static {
		MinecraftForge.EVENT_BUS.register(LevelTicker.class);
	}

	@SubscribeEvent
	public static void onTick(TickEvent.LevelTickEvent event) {
		LevelTicker[] pair = tickers.get(event.level.dimension());
		if (pair == null) {
			return;
		}
		LevelTicker ticker = pair[event.phase.ordinal()];
		if (ticker == null) {
			return;
		}
		ticker.level = event.level;
		Scheduler.tick(ticker);
	}

	@SubscribeEvent
	public static void unloadLevel(LevelEvent.Unload event) {
		if (!(event.getLevel() instanceof Level)) {
			return;
		}
		LevelTicker[] pair = tickers.get(((Level) event.getLevel()).dimension());
		if (pair == null) {
			return;
		}
		if (pair[0] != null) {
			pair[0].level = null;
		}
		if (pair[1] != null) {
			pair[1].level = null;
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
		LevelTicker[] pair = tickers.get(dimension);
		if (pair != null) {
			if (pair[0] == this) {
				pair[0] = null;
			}
			if (pair[1] == this) {
				pair[1] = null;
			}
		}
		level = null;
	}
}
