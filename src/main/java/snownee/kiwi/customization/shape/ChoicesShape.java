package snownee.kiwi.customization.shape;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import snownee.kiwi.customization.block.KBlockUtils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public record ChoicesShape(List<String> keys, Map<String, ShapeGenerator> valueMap) implements ShapeGenerator {
	public static <T extends Comparable<T>> ShapeGenerator chooseOneProperty(Property<T> property, Map<T, ShapeGenerator> valueMap) {
		return new ChoicesShape(
				List.of(property.getName()),
				property.getPossibleValues().stream().collect(Collectors.toUnmodifiableMap(property::getName, valueMap::get)));
	}

	public static ShapeGenerator chooseBooleanProperty(BooleanProperty property, ShapeGenerator trueShape, ShapeGenerator falseShape) {
		return new ChoicesShape(
				List.of(property.getName()),
				Map.of("true", trueShape, "false", falseShape));
	}

	@Override
	public VoxelShape getShape(BlockState blockState, CollisionContext context) {
		String key;
		if (keys.size() == 1) {
			key = KBlockUtils.getValueString(blockState, keys.get(0));
		} else {
			key = String.join(",", keys.stream().map(k -> KBlockUtils.getValueString(blockState, k)).toArray(String[]::new));
		}
		return valueMap.get(key).getShape(blockState, context);
	}

	public record Unbaked(List<String> keys, Map<String, UnbakedShape> choices) implements UnbakedShape {
		public static Codec<Unbaked> codec(UnbakedShapeCodec parentCodec) {
			return ExtraCodecs.validate(RecordCodecBuilder.create(instance -> instance.group(
					ExtraCodecs.NON_EMPTY_STRING.listOf().fieldOf("keys").forGetter(Unbaked::keys),
					Codec.unboundedMap(ExtraCodecs.NON_EMPTY_STRING, parentCodec).fieldOf("choices").forGetter(Unbaked::choices)
			).apply(instance, Unbaked::new)), $ -> {
				if ($.keys().isEmpty()) {
					return DataResult.error(() -> "Keys must not be empty");
				}
				if ($.choices().isEmpty()) {
					throw new IllegalArgumentException("Choices must not be empty");
				}
				return DataResult.success($);
			});
		}

		@Override
		public ShapeGenerator bake(BakingContext context) {
			return new ChoicesShape(
					keys,
					choices.entrySet()
							.stream()
							.collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, e -> e.getValue().bake(context))));
		}

		@Override
		public Stream<UnbakedShape> dependencies() {
			return choices.values().stream();
		}
	}
}
