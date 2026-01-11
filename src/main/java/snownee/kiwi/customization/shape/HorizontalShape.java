package snownee.kiwi.customization.shape;

import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.kiwi.util.VoxelUtil;

public record HorizontalShape(VoxelShape[] shapes) implements AbstractHorizontalShape {
	public static ShapeGenerator create(ShapeGenerator northGenerator) {
		VoxelShape north = Unit.unboxOrThrow(northGenerator);
		if (VoxelUtil.isIsotropicHorizontally(north)) {
			return northGenerator;
		}
		VoxelShape[] shapes = new VoxelShape[4];
		shapes[Direction.NORTH.get2DDataValue()] = north;
		return new HorizontalShape(shapes);
	}

	@Override
	public Direction getDirection(BlockState blockState) {
		return blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
	}

	public record Unbaked(UnbakedShape wrapped) implements UnbakedShape {
		public static Codec<Unbaked> codec(UnbakedShapeCodec parentCodec) {
			return RecordCodecBuilder.create(instance -> instance.group(
					parentCodec.fieldOf("north").forGetter(Unbaked::wrapped)
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
