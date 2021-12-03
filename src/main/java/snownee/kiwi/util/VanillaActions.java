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
import snownee.kiwi.mixin.AxeItemAccessor;
import snownee.kiwi.mixin.FireBlockAccessor;
import snownee.kiwi.mixin.HoeItemAccessor;
import snownee.kiwi.mixin.ShovelItemAccessor;
import snownee.kiwi.mixin.VillagerAccessor;
import snownee.kiwi.mixin.WorkAtComposterAccessor;

/**
 * @since 5.0.0
 */
public final class VanillaActions { //TODO brewing
	private VanillaActions() {
	}

	public static void setFireInfo(Block blockIn, int encouragement, int flammability) {
		((FireBlockAccessor) Blocks.FIRE).callSetFlammable(blockIn, encouragement, flammability);
	}

	public static void registerHoeConversion(Block k, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> v) {
		HoeItemAccessor.getTILLABLES().put(k, v);
	}

	public static void registerAxeConversion(Block k, Block v) {
		if (AxeItemAccessor.getSTRIPPABLES() instanceof ImmutableMap) {
			AxeItemAccessor.setSTRIPPABLES(Maps.newHashMap(AxeItemAccessor.getSTRIPPABLES()));
		}
		AxeItemAccessor.getSTRIPPABLES().put(k, v);
	}

	public static void registerShovelConversion(Block k, BlockState v) {
		if (ShovelItemAccessor.getFLATTENABLES() instanceof ImmutableMap) {
			ShovelItemAccessor.setFLATTENABLES( Maps.newHashMap(ShovelItemAccessor.getFLATTENABLES()));
		}
		ShovelItemAccessor.getFLATTENABLES().put(k, v);
	}

	public static void registerCompostable(float chance, ItemLike itemIn) {
		ComposterBlock.COMPOSTABLES.put(itemIn.asItem(), chance);
	}

	public static void registerVillagerPickupable(ItemLike item) {
		if (VillagerAccessor.getWANTED_ITEMS() instanceof ImmutableSet) {
			VillagerAccessor.setWANTED_ITEMS( Sets.newHashSet(VillagerAccessor.getWANTED_ITEMS()));
		}
		VillagerAccessor.getWANTED_ITEMS().add(item.asItem());
	}

	public static void registerVillagerCompostable(ItemLike item) {
		if (WorkAtComposterAccessor.getCOMPOSTABLE_ITEMS() instanceof ImmutableList) {
			WorkAtComposterAccessor.setCOMPOSTABLE_ITEMS(Lists.newArrayList(WorkAtComposterAccessor.getCOMPOSTABLE_ITEMS()));
		}
		WorkAtComposterAccessor.getCOMPOSTABLE_ITEMS().add(item.asItem());
	}

}
