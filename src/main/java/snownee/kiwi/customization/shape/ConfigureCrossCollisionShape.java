package snownee.kiwi.customization.shape;

import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.kiwi.mixin.customization.CrossCollisionBlockAccessor;

public record ConfigureCrossCollisionShape(
		float nodeWidth,
		float extensionWidth,
		float nodeHeight,
		float extensionBottom,
		float extensionHeight) implements ConfiguringShape {
	public static Codec<ConfigureCrossCollisionShape> codec() {
		return RecordCodecBuilder.create(instance -> instance.group(
				Codec.FLOAT.fieldOf("post_width").forGetter(ConfigureCrossCollisionShape::nodeWidth),
				Codec.FLOAT.fieldOf("side_width").forGetter(ConfigureCrossCollisionShape::extensionWidth),
				Codec.FLOAT.fieldOf("post_max_y").forGetter(ConfigureCrossCollisionShape::nodeHeight),
				Codec.FLOAT.fieldOf("side_min_y").forGetter(ConfigureCrossCollisionShape::extensionBottom),
				Codec.FLOAT.fieldOf("side_max_y").forGetter(ConfigureCrossCollisionShape::extensionHeight)
		).apply(instance, ConfigureCrossCollisionShape::new));
	}

	@Override
	public void configure(Block block, BlockShapeType type, ShapeStorage storage) {
		if (!(block instanceof CrossCollisionBlock crossCollisionBlock)) {
			throw new IllegalArgumentException("Block %s is not a CrossCollisionBlock".formatted(block));
		}
		Function<BlockState, VoxelShape> shapes = makeShapes(nodeWidth, extensionWidth, nodeHeight, extensionBottom, extensionHeight);
		CrossCollisionBlockAccessor accessor = (CrossCollisionBlockAccessor) crossCollisionBlock;
		switch (type) {
			case MAIN -> accessor.kiwi$setShapes(shapes);
			case COLLISION -> accessor.kiwi$setCollisionShapes(shapes);
			case INTERACTION -> throw new UnsupportedOperationException();
		}
	}

	private static Function<BlockState, VoxelShape> makeShapes(
			float nodeWidth,
			float extensionWidth,
			float nodeHeight,
			float extensionBottom,
			float extensionHeight) {
		float halfNodeWidth = nodeWidth / 2;
		float halfExtensionWidth = extensionWidth / 2;
		float nodeMin = 8.0F - halfNodeWidth;
		float nodeMax = 8.0F + halfNodeWidth;
		float extensionMin = 8.0F - halfExtensionWidth;
		float extensionMax = 8.0F + halfExtensionWidth;
		VoxelShape node = Block.box(nodeMin, 0.0, nodeMin, nodeMax, nodeHeight, nodeMax);
		VoxelShape north = Block.box(extensionMin, extensionBottom, 0.0, extensionMax, extensionHeight, extensionMax);
		VoxelShape south = Block.box(extensionMin, extensionBottom, extensionMin, extensionMax, extensionHeight, 16.0);
		VoxelShape west = Block.box(0.0, extensionBottom, extensionMin, extensionMax, extensionHeight, extensionMax);
		VoxelShape east = Block.box(extensionMin, extensionBottom, extensionMin, 16.0, extensionHeight, extensionMax);
		VoxelShape eastWest = Shapes.or(north, east);
		VoxelShape northSouth = Shapes.or(south, west);
		VoxelShape[] shapes = new VoxelShape[]{
				Shapes.empty(),
				south,
				west,
				northSouth,
				north,
				Shapes.or(south, north),
				Shapes.or(west, north),
				Shapes.or(northSouth, north),
				east,
				Shapes.or(south, east),
				Shapes.or(west, east),
				Shapes.or(northSouth, east),
				eastWest,
				Shapes.or(south, eastWest),
				Shapes.or(west, eastWest),
				Shapes.or(northSouth, eastWest)
		};

		for (int i = 0; i < shapes.length; i++) {
			shapes[i] = Shapes.or(node, shapes[i]);
		}
		return state -> shapes[getAabbIndex(state)];
	}

	private static int getAabbIndex(BlockState state) {
		int index = 0;
		if (state.getValue(CrossCollisionBlock.NORTH)) {
			index |= 1 << Direction.NORTH.get2DDataValue();
		}
		if (state.getValue(CrossCollisionBlock.EAST)) {
			index |= 1 << Direction.EAST.get2DDataValue();
		}
		if (state.getValue(CrossCollisionBlock.SOUTH)) {
			index |= 1 << Direction.SOUTH.get2DDataValue();
		}
		if (state.getValue(CrossCollisionBlock.WEST)) {
			index |= 1 << Direction.WEST.get2DDataValue();
		}
		return index;
	}

	@Override
	public void replaceAll(Block block, BlockShapeType type, UnaryOperator<VoxelShape> operator) {
		if (!(block instanceof CrossCollisionBlock crossCollisionBlock)) {
			throw new IllegalArgumentException("Block %s is not a CrossCollisionBlock".formatted(block));
		}
		CrossCollisionBlockAccessor accessor = (CrossCollisionBlockAccessor) crossCollisionBlock;
		Function<BlockState, VoxelShape> shapes = switch (type) {
			case MAIN -> accessor.kiwi$getShapes();
			case COLLISION -> accessor.kiwi$getCollisionShapes();
			case INTERACTION -> throw new UnsupportedOperationException();
		};
		Function<BlockState, VoxelShape> newShapes = state -> operator.apply(shapes.apply(state));
		switch (type) {
			case MAIN -> accessor.kiwi$setShapes(newShapes);
			case COLLISION -> accessor.kiwi$setCollisionShapes(newShapes);
			case INTERACTION -> throw new UnsupportedOperationException();
		}
	}
}
