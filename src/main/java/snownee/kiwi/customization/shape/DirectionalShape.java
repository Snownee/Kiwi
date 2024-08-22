package snownee.kiwi.customization.shape;

import java.util.stream.Stream;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.kiwi.util.VoxelUtil;

public record DirectionalShape(VoxelShape[] shapes, String property) implements ShapeGenerator {
	public static ShapeGenerator create(ShapeGenerator upGenerator, String property) {
		Preconditions.checkArgument(property.equals("facing") || property.equals("orientation"), "Unknown property: " + property);
		VoxelShape up = Unit.unboxOrThrow(upGenerator);
		if (Shapes.block() == up) {
			return upGenerator;
		}
		VoxelShape[] shapes = new VoxelShape[6];
		shapes[Direction.DOWN.get3DDataValue()] = VoxelUtil.rotate(up, Direction.UP);
		return new DirectionalShape(shapes, property);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, CollisionContext context) {
		Direction direction = switch (property) {
			case "facing" -> blockState.getValue(BlockStateProperties.FACING);
			case "orientation" -> JigsawBlock.getFrontFacing(blockState);
			default -> throw new IllegalArgumentException("Unknown property: " + property);
		};
		int index = direction.get3DDataValue();
		VoxelShape shape = shapes[index];
		if (shape == null) {
			synchronized (shapes) {
				shape = shapes[index];
				if (shape == null) {
					shapes[index] = shape = VoxelUtil.rotate(shapes[Direction.DOWN.get3DDataValue()], direction);
				}
			}
		}
		return shape;
	}

	public record Unbaked(UnbakedShape wrapped, String property) implements UnbakedShape {
		public static Codec<Unbaked> codec(UnbakedShapeCodec parentCodec) {
			return RecordCodecBuilder.create(instance -> instance.group(
					parentCodec.fieldOf("up").forGetter(Unbaked::wrapped),
					ExtraCodecs.NON_EMPTY_STRING.optionalFieldOf("property", "facing").forGetter(Unbaked::property)
			).apply(instance, Unbaked::new));
		}

		@Override
		public ShapeGenerator bake(BakingContext context) {
			return create(wrapped.bake(context), property);
		}

		@Override
		public Stream<UnbakedShape> dependencies() {
			return Stream.of(wrapped);
		}
	}
}
