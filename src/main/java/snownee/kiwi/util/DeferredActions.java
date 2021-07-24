package snownee.kiwi.util;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ComposterBlock;
import net.minecraft.block.FireBlock;
import net.minecraft.entity.ai.brain.task.FarmerWorkTask;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLModIdMappingEvent;
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

	public static void registerHoeConversion(Block k, BlockState v) {
		add(() -> {
			HoeItem.TILLABLES.put(k, v);
		});
	}

	public static void registerAxeConversion(Block k, Block v) {
		add(() -> {
			if (AxeItem.STRIPABLES instanceof ImmutableMap) {
				AxeItem.STRIPABLES = Maps.newHashMap(AxeItem.STRIPABLES);
			}
			AxeItem.STRIPABLES.put(k, v);
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

	public static void registerCompostable(float chance, IItemProvider itemIn) {
		add(() -> {
			ComposterBlock.COMPOSTABLES.put(itemIn.asItem(), chance);
		});
	}

	public static void registerVillagerPickupable(IItemProvider item) {
		add(() -> {
			if (VillagerEntity.WANTED_ITEMS instanceof ImmutableSet) {
				VillagerEntity.WANTED_ITEMS = Sets.newHashSet(VillagerEntity.WANTED_ITEMS);
			}
			VillagerEntity.WANTED_ITEMS.add(item.asItem());
		});
	}

	public static void registerVillagerCompostable(IItemProvider item) {
		add(() -> {
			if (FarmerWorkTask.COMPOSTABLE_ITEMS instanceof ImmutableList) {
				FarmerWorkTask.COMPOSTABLE_ITEMS = Lists.newArrayList(FarmerWorkTask.COMPOSTABLE_ITEMS);
			}
			FarmerWorkTask.COMPOSTABLE_ITEMS.add(item.asItem());
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
