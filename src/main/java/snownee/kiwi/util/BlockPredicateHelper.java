package snownee.kiwi.util;

import org.apache.commons.lang3.NotImplementedException;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.world.level.block.state.BlockState;

public class BlockPredicateHelper {

	public static boolean fastMatch(BlockPredicate predicate, BlockState blockstate/*, Supplier<BlockEntity> beGetter*/) {
		if (predicate == BlockPredicate.ANY) {
			return true;
		}
		if (predicate.tag != null && !blockstate.is(predicate.tag)) {
			return false;
		}
		if (predicate.blocks != null && !predicate.blocks.contains(blockstate.getBlock())) {
			return false;
		}
		if (!predicate.properties.matches(blockstate)) {
			return false;
		}
		if (predicate.nbt != NbtPredicate.ANY) {
//			BlockEntity blockentity = beGetter.get();
//			if (blockentity == null || !predicate.nbt.matches(blockentity.saveWithFullMetadata())) {
//				return false;
//			}
			throw new NotImplementedException();
		}
		return true;
	}

}