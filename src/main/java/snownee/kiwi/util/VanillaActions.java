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

import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.mixin.AxeItemAccess;
import snownee.kiwi.mixin.FireBlockAccess;
import snownee.kiwi.mixin.HoeItemAccess;
import snownee.kiwi.mixin.ShovelItemAccess;
import snownee.kiwi.mixin.VillagerAccess;
import snownee.kiwi.mixin.WorkAtComposterAccess;

/**
 * @since 5.0.0
 */
public final class VanillaActions { //TODO brewing
	private VanillaActions() {
	}

	public static void setFireInfo(Block blockIn, int encouragement, int flammability) {
		((FireBlockAccess) Blocks.FIRE).callSetFlammable(blockIn, encouragement, flammability);
	}

	public static void registerHoeConversion(Block k, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> v) {
		HoeItemAccess.getTILLABLES().put(k, v);
	}

	public static void registerAxeConversion(Block k, Block v) {
		if (AxeItemAccess.getSTRIPPABLES() instanceof ImmutableMap) {
			AxeItemAccess.setSTRIPPABLES(Maps.newHashMap(AxeItemAccess.getSTRIPPABLES()));
		}
		AxeItemAccess.getSTRIPPABLES().put(k, v);
	}

	public static void registerShovelConversion(Block k, BlockState v) {
		if (ShovelItemAccess.getFLATTENABLES() instanceof ImmutableMap) {
			ShovelItemAccess.setFLATTENABLES( Maps.newHashMap(ShovelItemAccess.getFLATTENABLES()));
		}
		ShovelItemAccess.getFLATTENABLES().put(k, v);
	}

	public static void registerCompostable(float chance, ItemLike itemIn) {
		ComposterBlock.COMPOSTABLES.put(itemIn.asItem(), chance);
	}

	public static void registerVillagerPickupable(ItemLike item) {
		if (VillagerAccess.getWANTED_ITEMS() instanceof ImmutableSet) {
			VillagerAccess.setWANTED_ITEMS( Sets.newHashSet(VillagerAccess.getWANTED_ITEMS()));
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
