package snownee.kiwi.customization.shape;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.kiwi.util.VoxelUtil;

public interface AbstractHorizontalShape extends ShapeGenerator {

	@Override
	default VoxelShape getShape(BlockState blockState, CollisionContext context) {
		Direction direction = getDirection(blockState);
		int index = direction.get2DDataValue();
		VoxelShape[] shapes = shapes();
		VoxelShape shape = shapes[index];
		if (shape == null) {
			synchronized (shapes) {
				shape = shapes[index];
				if (shape == null) {
					shape = VoxelUtil.rotateHorizontal(shapes[Direction.NORTH.get2DDataValue()], direction);
					shapes[index] = shape;
				}
			}
		}
		return shape;
	}

	VoxelShape[] shapes();

	Direction getDirection(BlockState blockState);
}
