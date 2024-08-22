package snownee.kiwi.customization.placement;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import snownee.kiwi.customization.block.KBlockUtils;
import snownee.kiwi.util.codec.CustomizationCodecs;
import snownee.kiwi.util.codec.KCodecs;

public record StatePropertiesPredicate(List<PropertyMatcher> properties) implements Predicate<BlockState> {

	public static final Codec<StatePropertiesPredicate> CODEC = Codec.compoundList(Codec.STRING, Codec.either(
			KCodecs.compactList(Codec.STRING),
			CustomizationCodecs.INT_BOUNDS)
	).xmap($ -> new StatePropertiesPredicate($.stream().map(pair -> {
		Optional<List<String>> strValues = pair.getSecond().left();
		return strValues.map(strings -> new PropertyMatcher(pair.getFirst(), Either.left(Set.copyOf(strings))))
				.orElseGet(() -> new PropertyMatcher(pair.getFirst(), Either.right(pair.getSecond().right().orElseThrow())));
	}).toList()), $ -> $.properties.stream().map(matcher -> Pair.of(matcher.key, matcher.value.mapLeft(List::copyOf))).toList());

	@Override
	public boolean test(BlockState blockState) {
		for (PropertyMatcher matcher : this.properties) {
			if (!matcher.test(blockState)) {
				return false;
			}
		}
		return true;
	}

	public boolean smartTest(BlockState baseState, BlockState targetState) {
		for (PropertyMatcher matcher : this.properties) {
			if (!matcher.smartTest(baseState, targetState)) {
				return false;
			}
		}
		return true;
	}

	public record PropertyMatcher(String key, Either<Set<String>, MinMaxBounds.Ints> value) {

		public boolean test(BlockState blockState) {
			Property<?> property = KBlockUtils.getProperty(blockState, key);
			Optional<Set<String>> strValues = value.left();
			boolean isInteger = property.getValueClass() == Integer.class;
			if (strValues.isEmpty() && !isInteger) {
				throw new IllegalStateException("Property value type mismatch");
			}
			//noinspection OptionalIsPresent
			if (strValues.isEmpty()) {
				return value.right().orElseThrow().matches((Integer) blockState.getValue(property));
			} else {
				return strValues.get().contains(KBlockUtils.getValueString(blockState, key));
			}
		}

		public boolean smartTest(BlockState baseState, BlockState targetState) {
			Property<?> property = KBlockUtils.getProperty(targetState, key);
			Optional<Set<String>> strValues = value.left();
			boolean isInteger = property.getValueClass() == Integer.class;
			if (strValues.isEmpty() && !isInteger) {
				throw new IllegalStateException("Property value type mismatch");
			}
			if (strValues.isEmpty()) {
				return value.right().orElseThrow().matches((Integer) targetState.getValue(property));
			} else {
				String targetValue = KBlockUtils.getValueString(targetState, key);
				for (String s : strValues.get()) {
					if (s.startsWith("@")) {
						String baseValue = KBlockUtils.getValueString(baseState, s.substring(1));
						if (baseValue.equals(targetValue)) {
							return true;
						}
					} else {
						if (s.equals(targetValue)) {
							return true;
						}
					}
				}
				return false;
			}
		}
	}
}
