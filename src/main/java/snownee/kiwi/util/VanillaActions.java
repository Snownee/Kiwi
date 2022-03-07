package snownee.kiwi.util;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;

import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.FlattenableBlockRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.fabricmc.fabric.api.registry.TillableBlockRegistry;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.mixin.VillagerAccess;
import snownee.kiwi.mixin.WorkAtComposterAccess;

/**
 * @since 5.0.0
 */
public final class VanillaActions { //TODO brewing
	private VanillaActions() {
	}

	public static void setFireInfo(Block blockIn, int spread, int burn) {
		FlammableBlockRegistry.getDefaultInstance().add(blockIn, burn, spread);
	}

	public static void registerHoeConversion(Block k, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> v) {
		TillableBlockRegistry.register(k, v.getFirst(), v.getSecond());
	}

	public static void registerAxeConversion(Block k, Block v) {
		StrippableBlockRegistry.register(k, v);
	}

	public static void registerShovelConversion(Block k, BlockState v) {
		FlattenableBlockRegistry.register(k, v);
	}

	public static void registerCompostable(float chance, ItemLike itemIn) {
		CompostingChanceRegistry.INSTANCE.add(itemIn, chance);
	}

	public static void registerVillagerPickupable(ItemLike item) {
		if (VillagerAccess.getWANTED_ITEMS() instanceof ImmutableSet) {
			VillagerAccess.setWANTED_ITEMS(Sets.newHashSet(VillagerAccess.getWANTED_ITEMS()));
		}
		VillagerAccess.getWANTED_ITEMS().add(item.asItem());
	}

	public static void registerVillagerCompostable(ItemLike item) {
		if (WorkAtComposterAccess.getCOMPOSTABLE_ITEMS() instanceof ImmutableList) {
			WorkAtComposterAccess.setCOMPOSTABLE_ITEMS(Lists.newArrayList(WorkAtComposterAccess.getCOMPOSTABLE_ITEMS()));
		}
		WorkAtComposterAccess.getCOMPOSTABLE_ITEMS().add(item.asItem());
	}

}
