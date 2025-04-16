package snownee.kiwi.customization.builder;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.longs.LongAVLTreeSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.customization.block.KBlockUtils;
import snownee.kiwi.util.NotNullByDefault;

@NotNullByDefault
public record BlockSpread(Type type, FacingLimitation facingLimitation, int maxDistance) {
	public static final Codec<BlockSpread> CODEC = Codec.withAlternative(
			RecordCodecBuilder.create(instance -> instance.group(
							Type.CODEC.fieldOf("type").forGetter(BlockSpread::type),
							StringRepresentable.fromEnum(FacingLimitation::values)
									.optionalFieldOf("facing_limit", FacingLimitation.None)
									.forGetter(BlockSpread::facingLimitation),
							ExtraCodecs.POSITIVE_INT.optionalFieldOf("max_distance", 16).forGetter(BlockSpread::maxDistance))
					.apply(instance, BlockSpread::create)),
			Type.CODEC.xmap(type -> BlockSpread.create(type, FacingLimitation.None, 16), BlockSpread::type));

	private static final Interner<BlockSpread> INTERNER = Interners.newStrongInterner();

	public static BlockSpread create(Type type, FacingLimitation facingLimitation, int maxDistance) {
		return INTERNER.intern(new BlockSpread(type, facingLimitation, maxDistance));
	}

	public List<BlockPos> collect(
			UseOnContext context,
			Predicate<BlockState> blockPredicate,
			@Nullable BiConsumer<BlockPos, BlockState> blockConsumer) {
		return collect(
				context.getLevel(),
				context.getClickedPos(),
				Objects.requireNonNull(context.getPlayer()),
				blockPredicate,
				blockConsumer);
	}

	public List<BlockPos> collect(
			BlockGetter level,
			BlockPos origin,
			Player player,
			Predicate<BlockState> blockPredicate,
			@Nullable BiConsumer<BlockPos, BlockState> blockConsumer) {
		BlockState originalBlock = level.getBlockState(origin);
		if (!blockPredicate.test(originalBlock)) {
			return List.of();
		}
		Direction direction = player.getDirection();
		Direction originalDirection = null;
		try {
			String s = KBlockUtils.getValueString(originalBlock, "facing");
			originalDirection = Direction.valueOf(s.toUpperCase(Locale.ENGLISH));
		} catch (Exception ignored) {
		}
		return switch (type) {
			case PLANE_Y -> {
				if (originalDirection == null || facingLimitation.test(originalDirection, direction)) {
					yield collectPlane(level, origin, blockPredicate, direction, direction.getClockWise(), blockConsumer);
				}
				yield List.of();
			}
			case PLANE_XZ -> {
				float yRot = player.getYRot() / 90F % 1F;
				boolean forcedDirection = yRot < 0.15F || yRot > 0.85F;
				List<BlockPos> list = List.of();
				if (originalDirection == null || facingLimitation.test(originalDirection, direction)) {
					list = collectPlane(level, origin, blockPredicate, direction, Direction.UP, blockConsumer);
				}
				List<BlockPos> list2 = List.of();
				if (!forcedDirection && (originalDirection == null || facingLimitation.test(originalDirection, direction.getClockWise()))) {
					list2 = collectPlane(level, origin, blockPredicate, direction.getClockWise(), Direction.UP, blockConsumer);
				}
				if (list.size() != list2.size()) {
					yield list.size() > list2.size() ? list : list2;
				}
				yield list;
			}
			case PLANE_XYZ -> collectPlaneXYZ(level, origin, blockPredicate, player);
		};
	}

	private List<BlockPos> collectPlaneXYZ(BlockGetter level, BlockPos origin, Predicate<BlockState> blockPredicate, Player player) {
		throw new NotImplementedException();
	}

	private List<BlockPos> collectPlane(
			BlockGetter level,
			BlockPos origin,
			Predicate<BlockState> blockPredicate,
			Direction direction,
			Direction direction2,
			@Nullable BiConsumer<BlockPos, BlockState> blockConsumer) {
		List<BlockPos> list = Lists.newArrayList(origin);
		PosIterator iterator = new PlacePosIterator(origin, maxDistance, direction, direction2);
		while (iterator.hasNext()) {
			BlockPos next = iterator.next();
			BlockState blockState = level.getBlockState(next);
			if (!blockPredicate.test(blockState)) {
				continue;
			}
//			if (statePropertiesPredicate.isPresent() && !statePropertiesPredicate.get().smartTest(originalBlock, blockState)) {
//				continue;
//			}
			if (blockConsumer != null) {
				blockConsumer.accept(next, blockState);
			}
			list.add(next);
			iterator.add(next, null);
		}
		return list;
	}

	public enum Type implements StringRepresentable {
		PLANE_Y, PLANE_XZ, PLANE_XYZ;

		public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

		@Override
		public String getSerializedName() {
			return name().toLowerCase(Locale.ENGLISH);
		}
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
			return Objects.requireNonNull(queue.poll());
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
		final Direction direction2;

		PlacePosIterator(BlockPos origin, int maxDistance, Direction direction, Direction direction2) {
			super(origin, maxDistance);
			this.direction = direction;
			this.direction2 = direction2;
		}

		@Override
		public Stream<BlockPos> listPossibleNext(BlockPos cur, @Nullable BlockPos from) {
			Stream.Builder<BlockPos> builder = Stream.builder();
			for (int i = -1; i <= 1; i++) {
				for (int j = -1; j <= 1; j++) {
					if (i == 0 && j == 0) {
						continue;
					}
					BlockPos next = cur.relative(direction, i).relative(direction2, j);
					if (!next.equals(from)) {
						builder.accept(next);
					}
				}
			}
			return builder.build();
		}
	}
}
