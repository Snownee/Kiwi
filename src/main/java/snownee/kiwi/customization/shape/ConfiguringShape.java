package snownee.kiwi.customization.shape;

import java.util.stream.Stream;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface ConfiguringShape extends ShapeGenerator, UnbakedShape {

	void configure(Block block, BlockShapeType type);

	@Override
	default VoxelShape getShape(BlockState blockState, CollisionContext context) {
		throw new UnsupportedOperationException("ConfiguringShape should not be used directly");
	}

	@Override
	default ShapeGenerator bake(BakingContext context) {
		return this;
	}

	@Override
	default Stream<UnbakedShape> dependencies() {
		return Stream.empty();
	}
}
