package snownee.kiwi.util;

import java.util.Optional;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import net.minecraft.advancements.predicates.BlockPredicate;
import net.minecraft.advancements.predicates.DataComponentMatchers;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockPredicateHelper {

	public static final BlockPredicate ANY = new BlockPredicate(
			Optional.empty(),
			Optional.empty(),
			Optional.empty(),
			DataComponentMatchers.ANY);

	public static boolean fastMatch(BlockPredicate predicate, BlockState blockstate, Supplier<@Nullable BlockEntity> beGetter) {
		if (predicate == ANY) {
			return true;
		}
		if (!predicate.matchesState(blockstate)) {
			return false;
		}
		if (predicate.nbt().isEmpty() && predicate.components().isEmpty()) {
			return true;
		}
		BlockEntity be = beGetter.get();
		return be != null && be.getLevel() != null && predicate.matchesBlockEntity(be.getLevel(), be);
	}
}
