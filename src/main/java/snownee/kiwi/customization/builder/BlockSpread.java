package snownee.kiwi.customization.builder;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.longs.LongAVLTreeSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.customization.block.KBlockUtils;
import snownee.kiwi.customization.placement.StatePropertiesPredicate;

public record BlockSpread(
		Type type,
		Optional<StatePropertiesPredicate> statePropertiesPredicate,
		FacingLimitation facingLimitation,
		int maxDistance) {


	public List<BlockPos> collect(UseOnContext context, Predicate<Block> blockPredicate) {
		return collect(context.getLevel(), context.getClickedPos(), Objects.requireNonNull(context.getPlayer()), blockPredicate);
	}

	public List<BlockPos> collect(
			BlockGetter level,
			BlockPos origin,
			Player player,
			Predicate<Block> blockPredicate
	) {
		BlockState originalBlock = level.getBlockState(origin);
		Direction direction = player.getDirection();
		Direction originalDirection = Direction.NORTH;
		try {
			String s = KBlockUtils.getValueString(originalBlock, "facing");
			originalDirection = Direction.valueOf(s.toUpperCase(Locale.ENGLISH));
		} catch (Exception ignored) {
		}
		return switch (type) {
			case PLANE_XZ -> {
				List<BlockPos> list = List.of();
				if (facingLimitation.test(originalDirection, direction)) {
					list = collectPlaneXZ(level, origin, originalBlock, direction);
					if (list.size() > 1) {
						yield list;
					}
				}
				if (facingLimitation.test(originalDirection, direction.getClockWise())) {
					List<BlockPos> list2 = collectPlaneXZ(level, origin, originalBlock, direction.getClockWise());
					if (list2.size() > list.size()) {
						yield list2;
					}
				}
				yield list;
			}
			case PLANE_XYZ -> collectPlaneXYZ(level, origin, originalBlock, player);
		};
	}

	private List<BlockPos> collectPlaneXYZ(BlockGetter level, BlockPos origin, BlockState originalBlock, Player player) {
		throw new NotImplementedException();
	}

	private List<BlockPos> collectPlaneXZ(BlockGetter level, BlockPos origin, BlockState originalBlock, Direction direction) {
		List<BlockPos> list = Lists.newArrayList(origin);
		PosIterator iterator = new PlacePosIterator(origin, maxDistance, direction);
		while (iterator.hasNext()) {
			BlockPos next = iterator.next();
			BlockState blockState = level.getBlockState(next);
			if (!blockState.is(originalBlock.getBlock())) {
				continue;
			}
			if (statePropertiesPredicate.isPresent() && !statePropertiesPredicate.get().smartTest(originalBlock, blockState)) {
				continue;
			}
			list.add(next);
			iterator.add(next, null);
		}
		return list;
	}

	public enum Type {
		PLANE_XZ, PLANE_XYZ
	}

	abstract static class PosIterator implements Iterator<BlockPos> {
		final LongSet visited = new LongAVLTreeSet();
		final Queue<BlockPos> queue = Lists.newLinkedList();
		final BlockPos origin;
		final int maxDistance;

		PosIterator(BlockPos origin, int maxDistance) {
			this.origin = origin;
			this.maxDistance = maxDistance;
		}

		@Override
		public boolean hasNext() {
			if (visited.isEmpty()) {
				add(origin, null);
			}
			return !queue.isEmpty();
		}

		@Override
		public BlockPos next() {
			return queue.poll();
		}

		public void add(BlockPos cur, @Nullable BlockPos from) {
			if (origin.distManhattan(cur) > maxDistance) {
				return;
			}
			visited.add(cur.asLong());
			listPossibleNext(cur, from).filter(pos -> {
				long l = pos.asLong();
				if (visited.contains(l)) {
					return false;
				}
				visited.add(l);
				return true;
			}).forEach(queue::add);
		}

		public abstract Stream<BlockPos> listPossibleNext(BlockPos cur, @Nullable BlockPos from);
	}

	static class PlacePosIterator extends PosIterator {
		final Direction direction;

		PlacePosIterator(BlockPos origin, int maxDistance, Direction direction) {
			super(origin, maxDistance);
			this.direction = direction;
		}

		@Override
		public Stream<BlockPos> listPossibleNext(BlockPos cur, @Nullable BlockPos from) {
			Stream.Builder<BlockPos> builder = Stream.builder();
			for (int i = -1; i <= 1; i++) {
				for (int j = -1; j <= 1; j++) {
					if (i == 0 && j == 0) {
						continue;
					}
					BlockPos next = cur.relative(direction, i).relative(Direction.UP, j);
					if (!next.equals(from)) {
						builder.accept(next);
					}
				}
			}
			return builder.build();
		}
	}
}
