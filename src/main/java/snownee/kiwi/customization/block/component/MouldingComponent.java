package snownee.kiwi.customization.block.component;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.StairsShape;
import snownee.kiwi.customization.block.KBlockSettings;
import snownee.kiwi.customization.block.loader.KBlockComponents;

public record MouldingComponent() implements KBlockComponent {
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final EnumProperty<StairsShape> SHAPE = BlockStateProperties.STAIRS_SHAPE;
	private static final MouldingComponent INSTANCE = new MouldingComponent();

	public static MouldingComponent getInstance() {
		return INSTANCE;
	}

	@Override
	public Type<?> type() {
		return KBlockComponents.MOULDING.getOrCreate();
	}

	@Override
	public void injectProperties(Block block, StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, SHAPE);
	}

	@Override
	public BlockState registerDefaultState(BlockState state) {
		return state.setValue(FACING, Direction.NORTH).setValue(SHAPE, StairsShape.STRAIGHT);
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState pState) {
		return true;
	}

	private StairsShape getShapeAt(BlockState ourState, BlockGetter pLevel, BlockPos pPos) {
		Direction ourFacing = ourState.getValue(FACING);
		BlockState theirState = pLevel.getBlockState(pPos.relative(ourFacing));
		Direction theirFacing;
		if (canBeConnected(ourState, theirState)) {
			theirFacing = theirState.getValue(FACING);
			if (theirFacing.getAxis() != ourFacing.getAxis() && canTakeShape(ourState, pLevel, pPos, theirFacing.getOpposite())) {
				if (theirFacing == ourFacing.getCounterClockWise()) {
					return StairsShape.OUTER_LEFT;
				}
				return StairsShape.OUTER_RIGHT;
			}
		}

		theirState = pLevel.getBlockState(pPos.relative(ourFacing.getOpposite()));
		if (canBeConnected(ourState, theirState)) {
			theirFacing = theirState.getValue(FACING);
			if (theirFacing.getAxis() != ourFacing.getAxis() && canTakeShape(ourState, pLevel, pPos, theirFacing)) {
				if (theirFacing == ourFacing.getCounterClockWise()) {
					return StairsShape.INNER_LEFT;
				}
				return StairsShape.INNER_RIGHT;
			}
		}
		return StairsShape.STRAIGHT;
	}

	private boolean canTakeShape(BlockState ourState, BlockGetter pLevel, BlockPos pPos, Direction pFace) {
		BlockState blockState = pLevel.getBlockState(pPos.relative(pFace));
		return !canBeConnected(ourState, blockState) || blockState.getValue(FACING) != ourState.getValue(FACING);
	}

	private boolean canBeConnected(BlockState ourState, BlockState theirState) {
		//TODO add a new field Optional<BlockPredicate> to check?
		return ourState.is(theirState.getBlock());
	}

	@Override
	public BlockState getStateForPlacement(KBlockSettings settings, BlockState state, BlockPlaceContext context) {
		if (settings.customPlacement) {
			return state;
		}
		BlockPos blockpos = context.getClickedPos();
		BlockState blockstate = state.setValue(FACING, context.getHorizontalDirection());
		blockstate = blockstate.setValue(SHAPE, getShapeAt(blockstate, context.getLevel(), blockpos));
		if (blockstate.canSurvive(context.getLevel(), context.getClickedPos())) {
			return blockstate;
		}
		return null;
	}

	@Override
	public BlockState updateShape(
			BlockState pState,
			Direction pDirection,
			BlockState pNeighborState,
			LevelAccessor pLevel,
			BlockPos pPos,
			BlockPos pNeighborPos) {
		if (pDirection.getAxis().isHorizontal()) {
			pState = pState.setValue(SHAPE, getShapeAt(pState, pLevel, pPos));
		}
		return pState;
	}

	@Override
	public @Nullable Direction getHorizontalFacing(BlockState blockState) {
		if (blockState.getValue(SHAPE) == StairsShape.STRAIGHT) {
			return blockState.getValue(FACING).getOpposite();
		}
		return null;
	}

	@Override
	public BlockState rotate(BlockState pState, Rotation pRotation) {
		return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState pState, Mirror pMirror) {
		Direction direction = pState.getValue(FACING);
		StairsShape stairsshape = pState.getValue(SHAPE);
		switch (pMirror) {
			case LEFT_RIGHT:
				if (direction.getAxis() == Direction.Axis.Z) {
					return switch (stairsshape) {
						case INNER_LEFT -> pState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_RIGHT);
						case INNER_RIGHT -> pState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_LEFT);
						case OUTER_LEFT -> pState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_RIGHT);
						case OUTER_RIGHT -> pState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_LEFT);
						case STRAIGHT -> pState.rotate(Rotation.CLOCKWISE_180);
					};
				}
				break;
			case FRONT_BACK:
				if (direction.getAxis() == Direction.Axis.X) {
					return switch (stairsshape) {
						case INNER_LEFT -> pState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_LEFT);
						case INNER_RIGHT -> pState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_RIGHT);
						case OUTER_LEFT -> pState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_RIGHT);
						case OUTER_RIGHT -> pState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_LEFT);
						case STRAIGHT -> pState.rotate(Rotation.CLOCKWISE_180);
					};
				}
		}
		return pState;
	}
}
