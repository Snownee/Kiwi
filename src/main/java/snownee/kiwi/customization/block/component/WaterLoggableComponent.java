package snownee.kiwi.customization.block.component;

import snownee.kiwi.customization.block.loader.KBlockComponents;

import com.google.common.base.Preconditions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import snownee.kiwi.customization.block.KBlockSettings;

public class WaterLoggableComponent implements KBlockComponent {
	private static final WaterLoggableComponent INSTANCE = new WaterLoggableComponent();

	public static WaterLoggableComponent getInstance() {
		return INSTANCE;
	}

	protected WaterLoggableComponent() {}

	@Override
	public Type<?> type() {
		return KBlockComponents.WATER_LOGGABLE.getOrCreate();
	}

	@Override
	public void injectProperties(Block block, StateDefinition.Builder<Block, BlockState> builder) {
		Preconditions.checkState(block instanceof SimpleWaterloggedBlock, "Block must implement CheckedWaterloggedBlock");
		builder.add(BlockStateProperties.WATERLOGGED);
	}

	@Override
	public BlockState registerDefaultState(BlockState state) {
		return state.setValue(BlockStateProperties.WATERLOGGED, false);
	}

	@Override
	public BlockState getStateForPlacement(KBlockSettings settings, BlockState state, BlockPlaceContext context) {
		FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
		return state.setValue(BlockStateProperties.WATERLOGGED, fluidState.getType().isSame(Fluids.WATER));
	}

	@Override
	public BlockState updateShape(
			BlockState pState,
			Direction pDirection,
			BlockState pNeighborState,
			LevelAccessor pLevel,
			BlockPos pPos,
			BlockPos pNeighborPos) {
		if (pState.getValue(BlockStateProperties.WATERLOGGED)) {
			pLevel.scheduleTick(pPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
		}
		return pState;
	}
}
