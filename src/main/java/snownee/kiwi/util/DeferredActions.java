package snownee.kiwi.util;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;

import net.minecraft.world.entity.ai.behavior.WorkAtComposter;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fmllegacy.event.lifecycle.FMLModIdMappingEvent;
import snownee.kiwi.Kiwi;

/**
 * @since 3.5.0
 */
public final class DeferredActions { //TODO brewing
	private static boolean executed;
	private static final Queue<Runnable> actions = new ConcurrentLinkedQueue<>();

	private DeferredActions() {
	}

	static {
		MinecraftForge.EVENT_BUS.addListener(DeferredActions::execute);
	}

	public static void add(Runnable action) {
		actions.add(action);
	}

	public static void setFireInfo(Block blockIn, int encouragement, int flammability) {
		add(() -> {
			((FireBlock) Blocks.FIRE).setFlammable(blockIn, encouragement, flammability);
		});
	}

	public static void registerHoeConversion(Block k, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> v) {
		add(() -> {
			HoeItem.TILLABLES.put(k, v);
		});
	}

	public static void registerAxeConversion(Block k, Block v) {
		add(() -> {
			if (AxeItem.STRIPPABLES instanceof ImmutableMap) {
				AxeItem.STRIPPABLES = Maps.newHashMap(AxeItem.STRIPPABLES);
			}
			AxeItem.STRIPPABLES.put(k, v);
		});
	}

	/**
	 * @since 3.5.2
	 */
	public static void registerShovelConversion(Block k, BlockState v) {
		add(() -> {
			if (ShovelItem.FLATTENABLES instanceof ImmutableMap) {
				ShovelItem.FLATTENABLES = Maps.newHashMap(ShovelItem.FLATTENABLES);
			}
			ShovelItem.FLATTENABLES.put(k, v);
		});
	}

	public static void registerCompostable(float chance, ItemLike itemIn) {
		add(() -> {
			ComposterBlock.COMPOSTABLES.put(itemIn.asItem(), chance);
		});
	}

	public static void registerVillagerPickupable(ItemLike item) {
		add(() -> {
			if (Villager.WANTED_ITEMS instanceof ImmutableSet) {
				Villager.WANTED_ITEMS = Sets.newHashSet(Villager.WANTED_ITEMS);
			}
			Villager.WANTED_ITEMS.add(item.asItem());
		});
	}

	public static void registerVillagerCompostable(ItemLike item) {
		add(() -> {
			if (WorkAtComposter.COMPOSTABLE_ITEMS instanceof ImmutableList) {
				WorkAtComposter.COMPOSTABLE_ITEMS = Lists.newArrayList(WorkAtComposter.COMPOSTABLE_ITEMS);
			}
			WorkAtComposter.COMPOSTABLE_ITEMS.add(item.asItem());
		});
	}

	private static void execute(FMLModIdMappingEvent event) {
		if (executed)
			return;
		executed = true;
		if (actions.isEmpty())
			return;
		Kiwi.logger.debug("Executing {} deferred actions", actions.size());
		actions.forEach(Runnable::run);
		actions.clear();
	}
}
