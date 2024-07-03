package snownee.kiwi.util;

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
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * @since 5.0.0
 */
public final class VanillaActions {
	private VanillaActions() {
	}

	public static void setFireInfo(Block blockIn, int spread, int burn) {
		((FireBlock) Blocks.FIRE).setFlammable(blockIn, spread, burn);
	}

	public static void registerHoeConversion(Block k, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> v) {
		//noinspection deprecation
		HoeItem.TILLABLES.put(k, v);
	}

	public static void registerAxeConversion(Block k, Block v) {
		if (AxeItem.STRIPPABLES instanceof ImmutableMap) {
			AxeItem.STRIPPABLES = Maps.newHashMap(AxeItem.STRIPPABLES);
		}
		AxeItem.STRIPPABLES.put(k, v);
	}

	public static void registerShovelConversion(Block k, BlockState v) {
		if (AxeItem.STRIPPABLES instanceof ImmutableMap) {
			AxeItem.STRIPPABLES = Maps.newHashMap(AxeItem.STRIPPABLES);
		}
		AxeItem.STRIPPABLES.put(k, v.getBlock());
	}

	public static void registerCompostable(float chance, ItemLike itemIn) {
		// do nothing. use DataMapProvider
	}

	public static void registerVillagerPickupable(ItemLike item) {
		if (Villager.WANTED_ITEMS instanceof ImmutableSet) {
			Villager.WANTED_ITEMS = Sets.newHashSet(Villager.WANTED_ITEMS);
		}
		Villager.WANTED_ITEMS.add(item.asItem());
	}

	public static void registerVillagerCompostable(ItemLike item) {
		if (WorkAtComposter.COMPOSTABLE_ITEMS instanceof ImmutableList) {
			WorkAtComposter.COMPOSTABLE_ITEMS = Lists.newArrayList(WorkAtComposter.COMPOSTABLE_ITEMS);
		}
		WorkAtComposter.COMPOSTABLE_ITEMS.add(item.asItem());
	}

	public static void registerVillagerFood(ItemLike item, int value) {
		if (Villager.FOOD_POINTS instanceof ImmutableMap) {
			Villager.FOOD_POINTS = Maps.newHashMap(Villager.FOOD_POINTS);
		}
		Villager.FOOD_POINTS.put(item.asItem(), value);
	}

}
