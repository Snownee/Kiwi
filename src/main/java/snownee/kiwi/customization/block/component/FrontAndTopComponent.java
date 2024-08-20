package snownee.kiwi.customization.block.component;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import snownee.kiwi.customization.block.KBlockSettings;
import snownee.kiwi.customization.block.loader.KBlockComponents;

public record FrontAndTopComponent() implements KBlockComponent {
	public static final EnumProperty<FrontAndTop> ORIENTATION = BlockStateProperties.ORIENTATION;
	private static final FrontAndTopComponent INSTANCE = new FrontAndTopComponent();

	public static FrontAndTopComponent getInstance() {
		return INSTANCE;
	}

	@Override
	public Type<?> type() {
		return KBlockComponents.FRONT_AND_TOP.getOrCreate();
	}

	@Override
	public void injectProperties(Block block, StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(ORIENTATION);
	}

	@Override
	public BlockState registerDefaultState(BlockState state) {
		return state.setValue(ORIENTATION, FrontAndTop.NORTH_UP);
	}

	@Override
	public @Nullable BlockState getStateForPlacement(KBlockSettings settings, BlockState state, BlockPlaceContext context) {
		if (settings.customPlacement) {
			return state;
		}
		Direction front = context.getClickedFace();
		Direction top;
		if (front.getAxis() == Direction.Axis.Y) {
			top = context.getHorizontalDirection();
		} else {
			top = Direction.UP;
		}
		return state.setValue(ORIENTATION, FrontAndTop.fromFrontAndTop(front, top));
	}

	@Override
	public Direction getHorizontalFacing(BlockState blockState) {
		FrontAndTop frontAndTop = blockState.getValue(ORIENTATION);
		if (frontAndTop.front().getAxis().isHorizontal()) {
			return frontAndTop.front();
		} else {
			return frontAndTop.top();
		}
	}

	@Override
	public BlockState rotate(BlockState pState, Rotation pRotation) {
		return pState.setValue(ORIENTATION, pRotation.rotation().rotate(pState.getValue(ORIENTATION)));
	}

	@Override
	public BlockState mirror(BlockState pState, Mirror pMirror) {
		return pState.setValue(ORIENTATION, pMirror.rotation().rotate(pState.getValue(ORIENTATION)));
	}
}
