package snownee.kiwi.customization.shape;

import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.kiwi.mixin.customization.WallBlockAccessor;

public record ConfigureWallShape(
		float postWidth,
		float sideWidth,
		float postMaxY,
		float sideMinY,
		float lowSideMaxY,
		float tallSideMaxY) implements ConfiguringShape {
	public static Codec<ConfigureWallShape> codec() {
		return RecordCodecBuilder.create(instance -> instance.group(
				Codec.FLOAT.fieldOf("post_width").forGetter(ConfigureWallShape::postWidth),
				Codec.FLOAT.fieldOf("side_width").forGetter(ConfigureWallShape::sideWidth),
				Codec.FLOAT.fieldOf("post_max_y").forGetter(ConfigureWallShape::postMaxY),
				Codec.FLOAT.fieldOf("side_min_y").forGetter(ConfigureWallShape::sideMinY),
				Codec.FLOAT.fieldOf("low_side_max_y").forGetter(ConfigureWallShape::lowSideMaxY),
				Codec.FLOAT.fieldOf("tall_side_max_y").forGetter(ConfigureWallShape::tallSideMaxY)
		).apply(instance, ConfigureWallShape::new));
	}

	@Override
	public void configure(Block block, BlockShapeType type, ShapeStorage storage) {
		if (!(block instanceof WallBlock wallBlock)) {
			throw new IllegalArgumentException("Block %s is not a WallBlock".formatted(block));
		}
		Function<BlockState, VoxelShape> shapes = makeShapes(
				wallBlock,
				postWidth,
				sideWidth,
				postMaxY,
				sideMinY,
				lowSideMaxY,
				tallSideMaxY);
		switch (type) {
			case MAIN -> wallBlock.shapes = shapes;
			case COLLISION -> wallBlock.collisionShapes = shapes;
			case INTERACTION -> throw new UnsupportedOperationException();
		}
	}

	public Function<BlockState, VoxelShape> makeShapes(
			WallBlock block,
			float postWidth,
			float sideWidth,
			float postMaxY,
			float sideMinY,
			float lowSideMaxY,
			float tallSideMaxY) {
		VoxelShape voxelshape = Block.column(postWidth, 0.0, postMaxY);
		Map<Direction, VoxelShape> map = Shapes.rotateHorizontal(Block.boxZ(sideWidth, sideMinY, lowSideMaxY, 0.0, 11.0));
		Map<Direction, VoxelShape> map1 = Shapes.rotateHorizontal(Block.boxZ(sideWidth, sideMinY, tallSideMaxY, 0.0, 11.0));
		return block.getShapeForEachState(
				blockState -> {
					VoxelShape voxelshape1 = blockState.getValue(WallBlock.UP) ? voxelshape : Shapes.empty();

					for (Map.Entry<Direction, EnumProperty<WallSide>> entry : WallBlock.PROPERTY_BY_DIRECTION.entrySet()) {
						voxelshape1 = Shapes.or(
								voxelshape1, switch (blockState.getValue(entry.getValue())) {
									case NONE -> Shapes.empty();
									case LOW -> map.get(entry.getKey());
									case TALL -> map1.get(entry.getKey());
								});
					}

					return voxelshape1;
				}, WallBlock.WATERLOGGED);
	}

	@Override
	public void replaceAll(Block block, BlockShapeType type, UnaryOperator<VoxelShape> operator) {
		if (!(block instanceof WallBlock wallBlock)) {
			throw new IllegalArgumentException("Block %s is not a WallBlock".formatted(block));
		}
		if (type == BlockShapeType.INTERACTION) {
			throw new UnsupportedOperationException("Interaction shapes cannot be replaced for WallBlock");
		}
		WallBlockAccessor accessor = (WallBlockAccessor) wallBlock;
		Function<BlockState, VoxelShape> shapes = switch (type) {
			case MAIN -> accessor.kiwi$getShapes();
			case COLLISION -> accessor.kiwi$getCollisionShapes();
			default -> throw new IllegalStateException();
		};
		Function<BlockState, VoxelShape> newShapes = state -> operator.apply(shapes.apply(state));
		switch (type) {
			case MAIN -> accessor.kiwi$setShapes(newShapes);
			case COLLISION -> accessor.kiwi$setCollisionShapes(newShapes);
			default -> throw new IllegalStateException();
		}
	}
}
