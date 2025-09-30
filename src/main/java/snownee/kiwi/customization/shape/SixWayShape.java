package snownee.kiwi.customization.shape;

import java.util.List;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.kiwi.util.VoxelUtil;

public record SixWayShape(VoxelShape[] shapes, VoxelShape base, VoxelShape trueDown, VoxelShape falseDown) implements ShapeGenerator {
	private static final List<BooleanProperty> DIRECTION_PROPERTIES = List.of(
			BlockStateProperties.DOWN,
			BlockStateProperties.UP,
			BlockStateProperties.NORTH,
			BlockStateProperties.SOUTH,
			BlockStateProperties.WEST,
			BlockStateProperties.EAST);

	public static ShapeGenerator create(ShapeGenerator base_, ShapeGenerator trueDown_, ShapeGenerator falseDown_) {
		VoxelShape base = Unit.unboxOrThrow(base_);
		VoxelShape trueDown = Unit.unboxOrThrow(trueDown_);
		VoxelShape falseDown = Unit.unboxOrThrow(falseDown_);
		Preconditions.checkArgument(!trueDown.isEmpty() || !falseDown.isEmpty());
		VoxelShape[] shapes = new VoxelShape[64]; // 2^6
		return new SixWayShape(shapes, base, trueDown, falseDown);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, CollisionContext context) {
		int index = 0;
		for (int i = 0; i < 6; i++) {
			if (blockState.getValue(DIRECTION_PROPERTIES.get(i))) {
				index |= 1 << i;
			}
		}
		VoxelShape shape = shapes[index];
		if (shape == null) {
			synchronized (shapes) {
				shape = shapes[index];
				if (shape == null) {
					shape = base;
					for (int i = 0; i < 6; i++) {
						var sideShape = VoxelUtil.rotate(
								(index & (1 << i)) != 0 ? trueDown : falseDown,
								Direction.from3DDataValue(i));
						shape = Shapes.joinUnoptimized(shape, sideShape, BooleanOp.OR);
					}
					shapes[index] = shape = shape.optimize();
				}
			}
		}
		return shape;
	}

	public record Unbaked(UnbakedShape base, UnbakedShape trueDown, UnbakedShape falseDown) implements UnbakedShape {
		public static Codec<Unbaked> codec(UnbakedShapeCodec parentCodec) {
			return RecordCodecBuilder.create(instance -> instance.group(
					parentCodec.fieldOf("base").forGetter(Unbaked::base),
					parentCodec.fieldOf("true_down").forGetter(Unbaked::trueDown),
					parentCodec.fieldOf("false_down").forGetter(Unbaked::falseDown)
			).apply(instance, Unbaked::new));
		}

		@Override
		public ShapeGenerator bake(BakingContext context) {
			return create(base.bake(context), trueDown.bake(context), falseDown.bake(context));
		}

		@Override
		public Stream<UnbakedShape> dependencies() {
			return Stream.of(base, trueDown, falseDown);
		}
	}
}
