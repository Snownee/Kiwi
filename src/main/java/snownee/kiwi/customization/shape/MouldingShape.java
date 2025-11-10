package snownee.kiwi.customization.shape;

import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.kiwi.util.VoxelUtil;

public record MouldingShape(VoxelShape[] shapes) implements ShapeGenerator {
	private static final int[] mapping = new int[5 * 4];

	static {
		prepareMapping();
	}

	private static void prepareMapping() {
		StairsShape[] stairsShapes = StairsShape.values();
		for (int i = 0; i < 5; i++) {
			StairsShape stairsShape = stairsShapes[i];
			int stairsShapeIndex = switch (stairsShape) {
				case STRAIGHT -> 0;
				case INNER_LEFT, INNER_RIGHT -> 1;
				case OUTER_LEFT, OUTER_RIGHT -> 2;
			};
			int rotationOffset = switch (stairsShape) {
				case INNER_LEFT, OUTER_LEFT -> 0;
				default -> 1;
			};
			for (int j = 0; j < 4; j++) {
				mapping[i * 4 + j] = stairsShapeIndex * 4 + (j + rotationOffset) % 4;
			}
		}
	}

	public static ShapeGenerator create(ShapeGenerator northStraightGenerator) {
		VoxelShape northStraight = Unit.unboxOrThrow(northStraightGenerator);
		VoxelShape northInner = Shapes.or(northStraight, VoxelUtil.rotateHorizontal(northStraight, Direction.NORTH.getClockWise()));
		VoxelShape northOuter = Shapes.join(
				northStraight,
				VoxelUtil.rotateHorizontal(northStraight, Direction.NORTH.getClockWise()),
				BooleanOp.AND);
		VoxelShape[] shapes = Stream.of(northStraight, northInner, northOuter)
				.flatMap($ -> Direction.Plane.HORIZONTAL.stream().map(direction -> VoxelUtil.rotateHorizontal($, direction)))
				.toArray(VoxelShape[]::new);
		VoxelShape[] mappedShapes = new VoxelShape[5 * 4];
		for (int i = 0; i < mappedShapes.length; i++) {
			mappedShapes[i] = shapes[mapping[i]];
		}
		return new MouldingShape(mappedShapes);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, CollisionContext context) {
		int shape = blockState.getValue(BlockStateProperties.STAIRS_SHAPE).ordinal();
		int facing = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING).get2DDataValue();
		return shapes[shape * 4 + facing];
	}

	public record Unbaked(UnbakedShape wrapped) implements UnbakedShape {
		public static Codec<Unbaked> codec(UnbakedShapeCodec parentCodec) {
			return RecordCodecBuilder.create(instance -> instance.group(
					parentCodec.fieldOf("north_straight").forGetter(Unbaked::wrapped)
			).apply(instance, Unbaked::new));
		}

		@Override
		public ShapeGenerator bake(BakingContext context) {
			return create(wrapped.bake(context));
		}

		@Override
		public Stream<UnbakedShape> dependencies() {
			return Stream.of(wrapped);
		}
	}
}
