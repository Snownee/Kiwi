package snownee.kiwi.customization.shape;

import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

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
				Codec.FLOAT.fieldOf("tall_side_max_y").forGetter(ConfigureWallShape::wallTallHeight)
		).apply(instance, ConfigureWallShape::new));
	}

	@Override
	public void configure(Block block, BlockShapeType type) {
		if (!(block instanceof WallBlock wallBlock)) {
			throw new IllegalArgumentException("Block %s is not a WallBlock".formatted(block));
		}
		Map<BlockState, VoxelShape> shapes = wallBlock.makeShapes(
				width / 2,
				depth / 2,
				wallPostHeight,
				wallMinY,
				wallLowHeight,
				wallTallHeight);
		switch (type) {
			case MAIN -> wallBlock.shapeByIndex = shapes;
			case COLLISION -> wallBlock.collisionShapeByIndex = shapes;
			case INTERACTION -> throw new UnsupportedOperationException();
		}
	}
}
