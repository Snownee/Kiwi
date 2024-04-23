package snownee.kiwi.customization.block.behavior;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public interface CanSurviveHandler {
	boolean isSensitiveSide(BlockState state, Direction side);

	boolean canSurvive(BlockState state, LevelReader world, BlockPos pos);

	static CanSurviveHandler checkFloor() {
		return Impls.CHECK_FLOOR;
	}

	static CanSurviveHandler checkCeiling() {
		return Impls.CHECK_CEILING;
	}

	static CanSurviveHandler checkFace(DirectionProperty property) {
		return Impls.CHECK_FACE.computeIfAbsent(property, key -> new CanSurviveHandler() {
			@Override
			public boolean isSensitiveSide(BlockState state, Direction side) {
				return side == state.getValue(key).getOpposite();
			}

			@Override
			public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
				Direction direction = state.getValue(key);
				BlockPos neighbor = pos.relative(direction);
				return world.getBlockState(neighbor).isFaceSturdy(world, neighbor, direction.getOpposite(), SupportType.RIGID);
			}
		});
	}

	static Compound any(List<CanSurviveHandler> handlers) {
		return new Compound(true, handlers);
	}

	static Compound all(List<CanSurviveHandler> handlers) {
		return new Compound(false, handlers);
	}

	final class Impls {
		private Impls() {
		}

		private static final CanSurviveHandler CHECK_FLOOR = new CanSurviveHandler() {
			@Override
			public boolean isSensitiveSide(BlockState state, Direction side) {
				return side == Direction.DOWN;
			}

			@Override
			public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
				return Block.canSupportRigidBlock(world, pos.below());
			}
		};

		private static final CanSurviveHandler CHECK_CEILING = new CanSurviveHandler() {
			@Override
			public boolean isSensitiveSide(BlockState state, Direction side) {
				return side == Direction.UP;
			}

			@Override
			public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
				pos = pos.above();
				return world.getBlockState(pos).isFaceSturdy(world, pos, Direction.DOWN, SupportType.RIGID);
			}
		};

		private static final Map<DirectionProperty, CanSurviveHandler> CHECK_FACE = Maps.newHashMap();
	}

	record Compound(boolean any, List<CanSurviveHandler> handlers) implements CanSurviveHandler {
		@Override
		public boolean isSensitiveSide(BlockState state, Direction side) {
			for (CanSurviveHandler handler : handlers) {
				if (handler.isSensitiveSide(state, side)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
			for (CanSurviveHandler handler : handlers) {
				if (handler.canSurvive(state, world, pos) == any) {
					return any;
				}
			}
			return !any;
		}
	}
}
