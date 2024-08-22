package snownee.kiwi.util;

import java.util.Optional;

import org.apache.commons.lang3.NotImplementedException;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.world.level.block.state.BlockState;

public class BlockPredicateHelper {

	public static final BlockPredicate ANY = new BlockPredicate(Optional.empty(), Optional.empty(), Optional.empty());

	public static boolean fastMatch(BlockPredicate predicate, BlockState blockstate/*, Supplier<BlockEntity> beGetter*/) {
		if (predicate == null) { // TODO Optional<BlockPredicate>
			return true;
		}
		if (predicate.blocks().isPresent() && !predicate.blocks().get().contains(blockstate.getBlockHolder())) {
			return false;
		}
		if (!predicate.properties().map(propPredicate -> propPredicate.matches(blockstate)).orElse(Boolean.TRUE)) {
			return false;
		}
		if (predicate.nbt().isPresent()) {
//			BlockEntity blockentity = beGetter.get();
//			if (blockentity == null || !predicate.nbt.matches(blockentity.saveWithFullMetadata())) {
//				return false;
//			}
			throw new NotImplementedException();
		}
		return true;
	}

}