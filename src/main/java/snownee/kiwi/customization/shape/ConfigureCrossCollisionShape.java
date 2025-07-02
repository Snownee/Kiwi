package snownee.kiwi.customization.shape;

import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

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
		Function<BlockState, VoxelShape> shapes = crossCollisionBlock.makeShapes(
				nodeWidth / 2,
				extensionWidth / 2,
				nodeHeight,
				extensionBottom,
				extensionHeight);
		switch (type) {
			case MAIN -> crossCollisionBlock.shapes = shapes;
			case COLLISION -> crossCollisionBlock.collisionShapes = shapes;
			case INTERACTION -> throw new UnsupportedOperationException();
		}
	}

	@Override
	public void replaceAll(Block block, BlockShapeType type, UnaryOperator<VoxelShape> operator) {
		if (!(block instanceof CrossCollisionBlock crossCollisionBlock)) {
			throw new IllegalArgumentException("Block %s is not a CrossCollisionBlock".formatted(block));
		}
		Function<BlockState, VoxelShape> shapes = switch (type) {
			case MAIN -> crossCollisionBlock.shapes;
			case COLLISION -> crossCollisionBlock.collisionShapes;
			default -> throw new IllegalStateException();
		};
		Function<BlockState, VoxelShape> newShapes = MergeConfiguredShape.transform(block, operator, shapes);
		switch (type) {
			case MAIN -> crossCollisionBlock.shapes = newShapes;
			case COLLISION -> crossCollisionBlock.collisionShapes = newShapes;
			default -> throw new IllegalStateException();
		}
	}
}
