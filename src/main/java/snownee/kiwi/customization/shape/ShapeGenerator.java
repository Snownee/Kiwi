package snownee.kiwi.customization.shape;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface ShapeGenerator {
	VoxelShape getShape(BlockState blockState, CollisionContext context);

	static ShapeGenerator unit(VoxelShape shape) {
		return new Unit(shape);
	}

	record Unit(VoxelShape shape) implements ShapeGenerator {
		public static VoxelShape unboxOrThrow(ShapeGenerator unit) {
			if (unit instanceof Unit) {
				return ((Unit) unit).shape;
			}
			throw new IllegalArgumentException("Not a unit shape");
		}

		@Override
		public VoxelShape getShape(BlockState blockState, CollisionContext context) {
			return shape;
		}
	}

	//	static ShapeGenerator faceAttached(ShapeGenerator floor, ShapeGenerator ceiling, ShapeGenerator wall) {
//		return choices(
//				BlockStateProperties.ATTACH_FACE,
//				Map.of(
//						AttachFace.FLOOR,
//						HorizontalShape.create(floor),
//						AttachFace.CEILING,
//						HorizontalShape.create(ceiling),
//						AttachFace.WALL,
//						HorizontalShape.create(wall)));
//	}
//
//	static ShapeGenerator layered(
//			LayeredComponent component,
//			Function<ResourceLocation, ShapeGenerator> shapeGetter,
//			ResourceLocation shapeId) {
//		IntegerProperty property = component.getLayerProperty();
//		int min = property.min;
//		int max = property.max;
//		ShapeGenerator[] shapes = new ShapeGenerator[max - min + 1];
//		for (int i = min; i <= max; i++) {
//			shapes[i - min] = shapeGetter.apply(shapeId.withSuffix("_" + i));
//		}
//		return (blockState, context) -> shapes[blockState.getValue(property) - property.min].getShape(blockState, context);
//	}

}
