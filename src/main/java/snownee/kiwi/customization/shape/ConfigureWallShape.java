package snownee.kiwi.customization.shape;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.kiwi.mixin.customization.WallBlockAccessor;

public record ConfigureWallShape(
		float width,
		float depth,
		float wallPostHeight,
		float wallMinY,
		float wallLowHeight,
		float wallTallHeight) implements ConfiguringShape {
	public static Codec<ConfigureWallShape> codec() {
		return RecordCodecBuilder.create(instance -> instance.group(
						Codec.FLOAT.fieldOf("post_width").forGetter(ConfigureWallShape::width),
						Codec.FLOAT.fieldOf("side_width").forGetter(ConfigureWallShape::depth),
						Codec.FLOAT.fieldOf("post_max_y").forGetter(ConfigureWallShape::wallPostHeight),
						Codec.FLOAT.fieldOf("side_min_y").forGetter(ConfigureWallShape::wallMinY),
						Codec.FLOAT.fieldOf("low_side_max_y").forGetter(ConfigureWallShape::wallLowHeight),
						Codec.FLOAT.fieldOf("tall_side_max_y").forGetter(ConfigureWallShape::wallTallHeight))
				.apply(instance, ConfigureWallShape::new));
	}

	@Override
	public void configure(Block block, BlockShapeType type, ShapeStorage storage) {
		if (!(block instanceof WallBlock wallBlock)) {
			throw new IllegalArgumentException("Block %s is not a WallBlock".formatted(block));
		}
		Function<BlockState, VoxelShape> shapes = makeShapes(wallBlock, width, depth, wallPostHeight, wallMinY, wallLowHeight, wallTallHeight);
		WallBlockAccessor accessor = (WallBlockAccessor) wallBlock;
		switch (type) {
			case MAIN -> accessor.kiwi$setShapes(shapes);
			case COLLISION -> accessor.kiwi$setCollisionShapes(shapes);
			case INTERACTION -> throw new UnsupportedOperationException();
		}
	}

	private static VoxelShape applyWallShape(VoxelShape shape, WallSide side, VoxelShape lowSide, VoxelShape tallSide) {
		return switch (side) {
			case TALL -> Shapes.or(shape, tallSide);
			case LOW -> Shapes.or(shape, lowSide);
			case NONE -> shape;
		};
	}

	private static Function<BlockState, VoxelShape> makeShapes(
			WallBlock wallBlock,
			float postWidth,
			float sideWidth,
			float postMaxY,
			float sideMinY,
			float lowSideMaxY,
			float tallSideMaxY) {
		float halfPostWidth = postWidth / 2;
		float halfSideWidth = sideWidth / 2;
		float postMin = 8.0F - halfPostWidth;
		float postMax = 8.0F + halfPostWidth;
		float sideMin = 8.0F - halfSideWidth;
		float sideMax = 8.0F + halfSideWidth;
		VoxelShape post = Block.box(postMin, 0.0, postMin, postMax, postMaxY, postMax);
		VoxelShape northLow = Block.box(sideMin, sideMinY, 0.0, sideMax, lowSideMaxY, sideMax);
		VoxelShape southLow = Block.box(sideMin, sideMinY, sideMin, sideMax, lowSideMaxY, 16.0);
		VoxelShape westLow = Block.box(0.0, sideMinY, sideMin, sideMax, lowSideMaxY, sideMax);
		VoxelShape eastLow = Block.box(sideMin, sideMinY, sideMin, 16.0, lowSideMaxY, sideMax);
		VoxelShape northTall = Block.box(sideMin, sideMinY, 0.0, sideMax, tallSideMaxY, sideMax);
		VoxelShape southTall = Block.box(sideMin, sideMinY, sideMin, sideMax, tallSideMaxY, 16.0);
		VoxelShape westTall = Block.box(0.0, sideMinY, sideMin, sideMax, tallSideMaxY, sideMax);
		VoxelShape eastTall = Block.box(sideMin, sideMinY, sideMin, 16.0, tallSideMaxY, sideMax);
		Map<BlockState, VoxelShape> shapes = new HashMap<>();
		for (BlockState state : wallBlock.getStateDefinition().getPossibleStates()) {
			VoxelShape shape = Shapes.empty();
			shape = applyWallShape(shape, state.getValue(WallBlock.EAST), eastLow, eastTall);
			shape = applyWallShape(shape, state.getValue(WallBlock.WEST), westLow, westTall);
			shape = applyWallShape(shape, state.getValue(WallBlock.NORTH), northLow, northTall);
			shape = applyWallShape(shape, state.getValue(WallBlock.SOUTH), southLow, southTall);
			if (state.getValue(WallBlock.UP)) {
				shape = Shapes.or(shape, post);
			}
			shapes.put(state, shape);
		}
		return state -> shapes.getOrDefault(state, Shapes.empty());
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
