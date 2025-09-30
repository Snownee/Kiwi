package snownee.kiwi.customization.block.component;

import java.util.List;

import com.google.common.collect.Iterables;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import snownee.kiwi.customization.block.KBlockSettings;
import snownee.kiwi.customization.block.loader.KBlockComponents;

public record HorizontalComponent(boolean oppose) implements KBlockComponent {
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	private static final HorizontalComponent NORMAL = new HorizontalComponent(false);
	private static final HorizontalComponent OPPOSE = new HorizontalComponent(true);
	public static final Codec<HorizontalComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("oppose", false).forGetter(HorizontalComponent::oppose)
	).apply(instance, HorizontalComponent::getInstance));

	public static HorizontalComponent getInstance(boolean oppose) {
		return oppose ? OPPOSE : NORMAL;
	}

	@Override
	public Type<?> type() {
		return KBlockComponents.HORIZONTAL.getOrCreate();
	}

	@Override
	public void injectProperties(Block block, StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState registerDefaultState(BlockState state) {
		return state.setValue(FACING, Direction.NORTH);
	}

	@Override
	public BlockState getStateForPlacement(KBlockSettings settings, BlockState state, BlockPlaceContext context) {
		if (settings.customPlacement) {
			return state;
		}
		Direction firstDirection = context.getHorizontalDirection();
		Iterable<Direction> directions = Iterables.concat(List.of(firstDirection), List.of(context.getNearestLookingDirections()));
		int index = 0;
		for (Direction direction : directions) {
			index += 1;
			if (direction.getAxis().isVertical()) {
				continue;
			}
			if (index > 1 && direction == firstDirection) {
				continue;
			}
			BlockState blockstate = state.setValue(FACING, oppose ? direction : direction.getOpposite());
			if (blockstate.canSurvive(context.getLevel(), context.getClickedPos())) {
				return blockstate;
			}
		}
		return null;
	}

	@Override
	public Direction getHorizontalFacing(BlockState blockState) {
		Direction direction = blockState.getValue(FACING);
		return oppose ? direction.getOpposite() : direction;
	}

	@Override
	public BlockState rotate(BlockState pState, Rotation pRotation) {
		return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState pState, Mirror pMirror) {
		//noinspection deprecation
		return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
	}
}
