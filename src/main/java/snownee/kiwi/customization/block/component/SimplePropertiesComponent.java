package snownee.kiwi.customization.block.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import snownee.kiwi.customization.block.KBlockUtils;
import snownee.kiwi.customization.block.StringProperty;
import snownee.kiwi.customization.block.loader.KBlockComponents;
import snownee.kiwi.util.codec.CustomizationCodecs;
import snownee.kiwi.util.codec.JavaOps;

public record SimplePropertiesComponent(
		boolean useShapeForLightOcclusion,
		List<Pair<Property<?>, String>> properties) implements KBlockComponent {
	//TODO check if this is working correctly
	private static final Interner<SimplePropertiesComponent> INTERNER = Interners.newStrongInterner();
	public static final Codec<Pair<Property<?>, String>> SINGLE_CODEC = new Codec<>() {
		private static final Map<String, Direction> DIRECTION_STRINGS = Direction.stream()
				.collect(Collectors.toUnmodifiableMap(StringRepresentable::getSerializedName, Function.identity()));

		@Override
		public <T> DataResult<Pair<Pair<Property<?>, String>, T>> decode(DynamicOps<T> ops, T input) {
			DataResult<MapLike<T>> mapValue = ops.getMap(input);
			if (mapValue.error().isPresent()) {
				return DataResult.error(mapValue.error().get()::message);
			}
			MapLike<T> map = mapValue.result().orElseThrow();
			DataResult<String> commonValue = ops.getStringValue(map.get("common"));
			Object defaultValue = ops.convertTo(JavaOps.INSTANCE, map.get("default"));
			List<String> values = Codec.STRING.listOf().parse(ops, map.get("values")).result().orElse(null);
			if (values != null && values.size() < 2) {
				return DataResult.error(() -> "Invalid values for property: " + values);
			}
			if (defaultValue == null) {
				if (values == null) {
					return DataResult.error(() -> "Missing default value for property");
				} else {
					defaultValue = values.get(0);
				}
			}
			Property<?> property;
			if (commonValue.result().isPresent()) {
				String s = commonValue.result().get();
				property = KBlockUtils.COMMON_PROPERTIES.get(s);
				if (property == null) {
					return DataResult.error(() -> "Unknown common property: " + s);
				}
			} else {
				String name = ops.getStringValue(map.get("name")).getOrThrow($ -> new IllegalStateException("Missing name for property"));

				if (defaultValue instanceof Integer) {
					int min = ops.getNumberValue(map.get("min")).getOrThrow($ -> new IllegalStateException(
							"Missing min for integer property")).intValue();
					int max = ops.getNumberValue(map.get("max")).getOrThrow($ -> new IllegalStateException(
							"Missing max for integer property")).intValue();
					property = IntegerProperty.create(name, min, max);
				} else if (defaultValue instanceof Boolean) { // will the NbtOps break this?
					property = BooleanProperty.create(name);
				} else if (values != null && defaultValue instanceof String s) {
					if (DIRECTION_STRINGS.containsKey(s) && DIRECTION_STRINGS.keySet().containsAll(values)) {
						if (values.size() == DIRECTION_STRINGS.size()) {
							property = DirectionProperty.create(name);
						} else {
							property = DirectionProperty.create(
									name,
									values.stream().map(DIRECTION_STRINGS::get).toArray(Direction[]::new));
						}
					} else {
						property = new StringProperty(name, values);
					}
				} else {
					String msg = "Unsupported default value type: " + defaultValue.getClass();
					return DataResult.error(() -> msg);
				}
				property = KBlockUtils.internProperty(property);
			}
			String defaultString;
			try {
				defaultString = KBlockUtils.getNameByValue(property, defaultValue);
				Preconditions.checkArgument(property.getValue(defaultString).isPresent());
			} catch (Exception e) {
				return DataResult.error(() -> "Invalid default value for property: " + e.getMessage());
			}
			return DataResult.success(Pair.of(Pair.of(property, defaultString), ops.empty()));
		}

		@Override
		public <T> DataResult<T> encode(Pair<Property<?>, String> input, DynamicOps<T> ops, T prefix) {
			RecordBuilder<T> mapBuilder = ops.mapBuilder();
			Property<?> property = input.getFirst();
			String s = KBlockUtils.COMMON_PROPERTIES.inverse().get(property);
			List<String> values = List.of();
			if (s == null) {
				mapBuilder.add("name", ops.createString(property.getName()));
				if (property instanceof IntegerProperty integerProperty) {
					mapBuilder.add("min", ops.createInt(integerProperty.min));
					mapBuilder.add("max", ops.createInt(integerProperty.max));
				} else if (property instanceof EnumProperty<?> || property instanceof StringProperty) {
					values = property.getPossibleValues()
							.stream()
							.map($ -> KBlockUtils.getNameByValue(property, $))
							.collect(Collectors.toCollection(ArrayList::new));
				} else if (!(property instanceof BooleanProperty)) {
					return DataResult.error(() -> "Unsupported property type: " + property);
				}
			} else {
				mapBuilder.add("common", ops.createString(s));
			}
			T defaultValue;
			if (property instanceof IntegerProperty integerProperty) {
				defaultValue = integerProperty.getValue(input.getSecond()).map(ops::createInt).orElse(null);
			} else if (property instanceof BooleanProperty booleanProperty) {
				defaultValue = booleanProperty.getValue(input.getSecond()).map(ops::createBoolean).orElse(null);
			} else {
				defaultValue = property.getValue(input.getSecond()).isPresent() ? ops.createString(input.getSecond()) : null;
			}
			if (defaultValue == null) {
				return DataResult.error(() -> "Invalid value %s for property %s".formatted(input.getSecond(), property.getName()));
			}
			if (!values.isEmpty()) {
				values.remove(input.getSecond());
				values.add(0, input.getSecond());
				mapBuilder.add("values", ops.createList(values.stream().map(ops::createString)));
			} else {
				mapBuilder.add("default", defaultValue);
			}
			return mapBuilder.build(prefix);
		}
	};
	public static final MapCodec<SimplePropertiesComponent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("shape_for_light_occlusion", false)
					.forGetter(SimplePropertiesComponent::useShapeForLightOcclusion),
			ExtraCodecs.nonEmptyList(CustomizationCodecs.compactList(SINGLE_CODEC))
					.fieldOf("properties")
					.forGetter(SimplePropertiesComponent::properties)
	).apply(instance, ($1, $2) -> INTERNER.intern(new SimplePropertiesComponent($1, $2))));

	@Override
	public Type<?> type() {
		return KBlockComponents.SIMPLE_PROPERTIES.getOrCreate();
	}

	@Override
	public void injectProperties(Block block, StateDefinition.Builder<Block, BlockState> builder) {
		for (Pair<Property<?>, String> pair : properties) {
			builder.add(pair.getFirst());
		}
	}

	@Override
	public BlockState registerDefaultState(BlockState state) {
		for (Pair<Property<?>, String> pair : properties) {
			state = KBlockUtils.setValueByString(state, pair.getFirst().getName(), pair.getSecond());
		}
		return state;
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState pState) {
		return useShapeForLightOcclusion;
	}
}
