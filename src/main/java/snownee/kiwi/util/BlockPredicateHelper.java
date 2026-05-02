package snownee.kiwi.util;

import java.util.Optional;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.advancements.criterion.DataComponentMatchers;
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
		if (predicate.blocks().isPresent() && !predicate.blocks().get().contains(blockstate.typeHolder())) {
			return false;
		}
		if (!predicate.properties().map(propPredicate -> propPredicate.matches(blockstate)).orElse(Boolean.TRUE)) {
			return false;
		}
		BlockEntity be = predicate.nbt().isPresent() || !predicate.components().isEmpty() ? beGetter.get() : null;
		if (predicate.nbt().isPresent()) {
			if (be == null || be.getLevel() == null || BlockPredicate.matchesBlockEntity(
					be.getLevel(),
					be,
					predicate.nbt().orElseThrow())) {
				return false;
			}
		}
		return predicate.components().isEmpty() || BlockPredicate.matchesComponents(be, predicate.components());
	}
}