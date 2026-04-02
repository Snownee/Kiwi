package snownee.kiwi.customization.shape;

import java.util.Objects;
import java.util.function.UnaryOperator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public record MergeConfiguredShape(ConfiguringShape configuring, Identifier additional) implements ConfiguringShape {
	public static Codec<MergeConfiguredShape> codec(UnbakedShapeCodec parentCodec) {
		return RecordCodecBuilder.create(i -> i.group(
						parentCodec.fieldOf("configuring").forGetter(MergeConfiguredShape::configuring),
						Identifier.CODEC.fieldOf("additional").forGetter(MergeConfiguredShape::additional)
				)
				.apply(i, (configuring, additional) -> new MergeConfiguredShape((ConfiguringShape) configuring, additional)));
	}

	@Override
	public void configure(Block block, BlockShapeType type, ShapeStorage storage) {
		configuring.configure(block, type, storage);
		VoxelShape shape = Unit.unboxOrThrow(Objects.requireNonNull(storage.get(additional)));
		replaceAll(block, type, original -> Shapes.or(original, shape));
	}

	@Override
	public void replaceAll(Block block, BlockShapeType type, UnaryOperator<VoxelShape> operator) {
		configuring.replaceAll(block, type, operator);
	}
}
