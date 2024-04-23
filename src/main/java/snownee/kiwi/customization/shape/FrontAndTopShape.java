package snownee.kiwi.customization.shape;

import java.util.Map;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class FrontAndTopShape {
	public static ShapeGenerator create(ShapeGenerator floor, ShapeGenerator ceiling, ShapeGenerator wall) {
		return ChoicesShape.chooseOneProperty(
				BlockStateProperties.ATTACH_FACE,
				Map.of(
						AttachFace.FLOOR,
						HorizontalShape.create(floor),
						AttachFace.CEILING,
						HorizontalShape.create(ceiling),
						AttachFace.WALL,
						HorizontalShape.create(wall)));
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
