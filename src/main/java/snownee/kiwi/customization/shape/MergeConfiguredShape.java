package snownee.kiwi.customization.shape;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public record MergeConfiguredShape(ConfiguringShape configuring, ResourceLocation additional) implements ConfiguringShape {
	public static Codec<MergeConfiguredShape> codec(UnbakedShapeCodec parentCodec) {
		return RecordCodecBuilder.create(i -> i.group(
						parentCodec.fieldOf("configuring").forGetter(MergeConfiguredShape::configuring),
						ResourceLocation.CODEC.fieldOf("additional").forGetter(MergeConfiguredShape::additional)
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

	public static Function<BlockState, VoxelShape> transform(
			Block block,
			UnaryOperator<VoxelShape> operator,
			Function<BlockState, VoxelShape> original) {
		List<BlockState> possibleStates = block.getStateDefinition().getPossibleStates();
		Map<VoxelShape, VoxelShape> cache = Maps.newHashMapWithExpectedSize(possibleStates.size());
		ImmutableMap.Builder<BlockState, VoxelShape> builder = ImmutableMap.builder();
		for (BlockState state : possibleStates) {
			VoxelShape originalShape = original.apply(state);
			VoxelShape newShape = cache.computeIfAbsent(originalShape, operator);
			builder.put(state, newShape);
		}
		return builder.build()::get;
	}
}