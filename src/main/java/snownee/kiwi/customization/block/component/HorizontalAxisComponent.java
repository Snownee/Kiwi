package snownee.kiwi.customization.block.component;

import snownee.kiwi.customization.block.loader.KBlockComponents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import snownee.kiwi.customization.block.KBlockSettings;

public record HorizontalAxisComponent(boolean oppose) implements KBlockComponent {
	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
	private static final HorizontalAxisComponent NORMAL = new HorizontalAxisComponent(false);
	private static final HorizontalAxisComponent OPPOSE = new HorizontalAxisComponent(true);
	public static final Codec<HorizontalAxisComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("oppose", false).forGetter(HorizontalAxisComponent::oppose)
	).apply(instance, HorizontalAxisComponent::getInstance));

	public static HorizontalAxisComponent getInstance(boolean oppose) {
		return oppose ? OPPOSE : NORMAL;
	}

	@Override
	public Type<?> type() {
		return KBlockComponents.HORIZONTAL_AXIS.getOrCreate();
	}

	@Override
	public void injectProperties(Block block, StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AXIS);
	}

	@Override
	public BlockState registerDefaultState(BlockState state) {
		return state.setValue(AXIS, Direction.Axis.X);
	}

	@Override
	public BlockState getStateForPlacement(KBlockSettings settings, BlockState state, BlockPlaceContext context) {
		if (settings.customPlacement) {
			return state;
		}
		for (Direction direction : context.getNearestLookingDirections()) {
			if (direction.getAxis().isVertical()) {
				continue;
			}
			BlockState blockstate = state.setValue(AXIS, (oppose ? direction : direction.getClockWise()).getAxis());
			if (blockstate.canSurvive(context.getLevel(), context.getClickedPos())) {
				return blockstate;
			}
		}
		return null;
	}

	public BlockState rotate(BlockState pState, Rotation pRot) {
		switch (pRot) {
			case COUNTERCLOCKWISE_90:
			case CLOCKWISE_90:
				switch (pState.getValue(AXIS)) {
					case Z:
						return pState.setValue(AXIS, Direction.Axis.X);
					case X:
						return pState.setValue(AXIS, Direction.Axis.Z);
					default:
						return pState;
				}
			default:
				return pState;
		}
	}
}
