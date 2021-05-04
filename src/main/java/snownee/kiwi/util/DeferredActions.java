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
			((FireBlock) Blocks.FIRE).setFireInfo(blockIn, encouragement, flammability);
		});
	}

	public static void registerHoeConversion(Block k, BlockState v) {
		add(() -> {
			HoeItem.HOE_LOOKUP.put(k, v);
		});
	}

	public static void registerAxeConversion(Block k, Block v) {
		add(() -> {
			if (AxeItem.BLOCK_STRIPPING_MAP instanceof ImmutableMap) {
				AxeItem.BLOCK_STRIPPING_MAP = Maps.newHashMap(AxeItem.BLOCK_STRIPPING_MAP);
			}
			AxeItem.BLOCK_STRIPPING_MAP.put(k, v);
		});
	}

	public static void registerCompostable(float chance, IItemProvider itemIn) {
		add(() -> {
			ComposterBlock.CHANCES.put(itemIn.asItem(), chance);
		});
	}

	public static void registerVillagerPickupable(IItemProvider item) {
		add(() -> {
			if (VillagerEntity.ALLOWED_INVENTORY_ITEMS instanceof ImmutableSet) {
				VillagerEntity.ALLOWED_INVENTORY_ITEMS = Sets.newHashSet(VillagerEntity.ALLOWED_INVENTORY_ITEMS);
			}
			VillagerEntity.ALLOWED_INVENTORY_ITEMS.add(item.asItem());
		});
	}

	public static void registerVillagerCompostable(IItemProvider item) {
		add(() -> {
			if (FarmerWorkTask.field_234014_b_ instanceof ImmutableList) {
				FarmerWorkTask.field_234014_b_ = Lists.newArrayList(FarmerWorkTask.field_234014_b_);
			}
			FarmerWorkTask.field_234014_b_.add(item.asItem());
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
