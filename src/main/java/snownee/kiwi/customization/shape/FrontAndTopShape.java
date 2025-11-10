package snownee.kiwi.customization.shape;

import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.kiwi.util.VoxelUtil;

public record FrontAndTopShape(ShapeGenerator floor, ShapeGenerator ceiling, ShapeGenerator wall) implements ShapeGenerator {
	public static ShapeGenerator create(ShapeGenerator floor, ShapeGenerator ceiling, ShapeGenerator wall) {
		return new FrontAndTopShape(Child.create(floor), Child.create(ceiling), Child.create(wall));
	}

	@Override
	public VoxelShape getShape(BlockState blockState, CollisionContext context) {
		FrontAndTop frontAndTop = blockState.getValue(BlockStateProperties.ORIENTATION);
		return switch (frontAndTop.front()) {
			case UP -> ceiling.getShape(blockState, context);
			case DOWN -> floor.getShape(blockState, context);
			default -> wall.getShape(blockState, context);
		};
	}

	public record Child(VoxelShape[] shapes) implements AbstractHorizontalShape {
		public static ShapeGenerator create(ShapeGenerator northGenerator) {
			VoxelShape north = Unit.unboxOrThrow(northGenerator);
			if (VoxelUtil.isIsotropicHorizontally(north)) {
				return northGenerator;
			}
			VoxelShape[] shapes = new VoxelShape[4];
			shapes[Direction.NORTH.get2DDataValue()] = north;
			return new Child(shapes);
		}

		@Override
		public Direction getDirection(BlockState blockState) {
			FrontAndTop frontAndTop = blockState.getValue(BlockStateProperties.ORIENTATION);
			return frontAndTop.top().getAxis().isHorizontal() ? frontAndTop.top() : frontAndTop.front();
		}
	}

	public record Unbaked(UnbakedShape floor, UnbakedShape ceiling, UnbakedShape wall) implements UnbakedShape {
		public static Codec<Unbaked> codec(UnbakedShapeCodec parentCodec) {
			return RecordCodecBuilder.create(instance -> instance.group(
					parentCodec.fieldOf("floor").forGetter(Unbaked::floor),
					parentCodec.fieldOf("ceiling").forGetter(Unbaked::ceiling),
					parentCodec.fieldOf("wall").forGetter(Unbaked::wall)
			).apply(instance, Unbaked::new));
		}

		@Override
		public ShapeGenerator bake(BakingContext context) {
			return create(floor.bake(context), ceiling.bake(context), wall.bake(context));
		}

		@Override
		public Stream<UnbakedShape> dependencies() {
			return Stream.of(floor, ceiling, wall);
		}
	}
}
