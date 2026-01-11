package snownee.kiwi.customization.block.component;

import org.jetbrains.annotations.Nullable;

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

public record DirectionalComponent(boolean oppose) implements KBlockComponent {
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	private static final DirectionalComponent NORMAL = new DirectionalComponent(false);
	private static final DirectionalComponent OPPOSE = new DirectionalComponent(true);
	public static final Codec<DirectionalComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("oppose", false).forGetter(DirectionalComponent::oppose)
	).apply(instance, DirectionalComponent::getInstance));

	public static DirectionalComponent getInstance(boolean oppose) {
		return oppose ? OPPOSE : NORMAL;
	}

	@Override
	public Type<?> type() {
		return KBlockComponents.DIRECTIONAL.getOrCreate();
	}

	@Override
	public void injectProperties(Block block, StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState registerDefaultState(BlockState state) {
		return state.setValue(FACING, Direction.DOWN);
	}

	@Override
	public @Nullable BlockState getStateForPlacement(KBlockSettings settings, BlockState state, BlockPlaceContext context) {
		if (settings.customPlacement) {
			return state;
		}
		for (Direction direction : context.getNearestLookingDirections()) {
			BlockState blockstate = state.setValue(FACING, oppose ? direction : direction.getOpposite());
			if (blockstate.canSurvive(context.getLevel(), context.getClickedPos())) {
				return blockstate;
			}
		}
		return null;
	}

	@Override
	public @Nullable Direction getHorizontalFacing(BlockState blockState) {
		Direction direction = blockState.getValue(FACING);
		if (direction.getAxis().isHorizontal()) {
			return oppose ? direction.getOpposite() : direction;
		}
		return null;
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
