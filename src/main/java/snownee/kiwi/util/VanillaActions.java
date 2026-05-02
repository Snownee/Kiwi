package snownee.kiwi.util;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;

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

	@SuppressWarnings("deprecation")
	public static void registerHoeConversion(Block k, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> v) {
		HoeItem.TILLABLES.put(k, v);
	}

	@SuppressWarnings("deprecation")
	public static void registerAxeConversion(Block k, Block v) {
		if (AxeItem.STRIPPABLES instanceof ImmutableMap) {
			AxeItem.STRIPPABLES = Maps.newHashMap(AxeItem.STRIPPABLES);
		}
		AxeItem.STRIPPABLES.put(k, v);
	}

	@SuppressWarnings("deprecation")
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
		// 26.1 no longer exposes a mutable wanted-items hook.
	}

	public static void registerVillagerCompostable(ItemLike item) {
		// 26.1 data-driven villager compost rules should be handled via data generation.
	}

	public static void registerVillagerFood(ItemLike item, int value) {
		// 26.1 FOOD_POINTS is immutable; keep runtime behavior data-driven instead.
	}

}
