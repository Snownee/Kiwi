package snownee.kiwi.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.Lists;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import snownee.kiwi.Kiwi;

public class AlternativesIngredient implements CustomIngredient {
	public static final Identifier ID = Kiwi.id("alternatives");
	private List<@Nullable Ingredient> options;
	private @Nullable Ingredient wrapped;
	private @Nullable Boolean requiresTesting;

	public AlternativesIngredient(@Nullable Ingredient wrapped) {
		this.wrapped = wrapped;
		this.options = List.of();
	}

	public AlternativesIngredient(List<@Nullable Ingredient> options) {
		if (options.isEmpty()) {
			throw new IllegalArgumentException("Options cannot be empty");
		}
		this.options = new ArrayList<>(options);
	}

	@Override
	public boolean test(ItemStack stack) {
		init();
		return wrapped != null && wrapped.test(stack);
	}

	@Override
	public List<ItemStack> getMatchingStacks() {
		init();
		if (wrapped == null) {
			return List.of();
		}
		@SuppressWarnings("deprecation")
		Stream<Holder<Item>> items = wrapped.items();
		return items.map(Holder::value).map(Item::getDefaultInstance).toList();
	}

	@Override
	public boolean requiresTesting() {
		if (requiresTesting == null) {
			requiresTesting = options.isEmpty() ? wrapped != null && wrapped.getCustomIngredient() != null :
					options.stream().anyMatch(option -> option != null && option.getCustomIngredient() != null);
		}
		return requiresTesting;
	}

	@Override
	public SlotDisplay display() {
		init();
		return wrapped != null ? wrapped.display() : SlotDisplay.Empty.INSTANCE;
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return Serializer.INSTANCE;
	}

	private void init() {
		if (wrapped != null || options.isEmpty()) {
			return;
		}
		try {
			for (Ingredient option : options) {
				if (option != null && !option.isEmpty()) {
					wrapped = option;
					break;
				}
			}
		} catch (Exception e) {
			Kiwi.LOGGER.error("Failed to initialize AlternativesIngredient {}", options, e);
		} finally {
			options = List.of();
		}
	}

	public static class Serializer extends MapCodec<AlternativesIngredient> implements CustomIngredientSerializer<AlternativesIngredient> {
		public static final Serializer INSTANCE = new Serializer();
		public static final StreamCodec<RegistryFriendlyByteBuf, AlternativesIngredient> STREAM_CODEC = StreamCodec.of(
				Serializer::write,
				Serializer::read);

		@Override
		public Identifier getIdentifier() {
			return ID;
		}

		@Override
		public MapCodec<AlternativesIngredient> getCodec(boolean allowEmpty) {
			return this;
		}

		public static AlternativesIngredient read(RegistryFriendlyByteBuf buf) {
			return new AlternativesIngredient(Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.decode(buf).orElse(null));
		}

		public static void write(RegistryFriendlyByteBuf buf, AlternativesIngredient ingredient) {
			ingredient.init();
			Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.encode(buf, Optional.ofNullable(ingredient.wrapped));
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, AlternativesIngredient> getPacketCodec() {
			return STREAM_CODEC;
		}

		@Override
		public <T> Stream<T> keys(DynamicOps<T> ops) {
			return Stream.of(ops.createString("options"));
		}

		@Override
		public <T> DataResult<AlternativesIngredient> decode(DynamicOps<T> ops, MapLike<T> input) {
			T rawOptions = input.get("options");
			if (rawOptions == null) {
				return DataResult.error(() -> "No valid ingredient found: missing options");
			}
			DataResult<Stream<T>> streamResult = ops.getStream(rawOptions);
			if (streamResult.isError()) {
				return DataResult.error(() -> "No valid ingredient found: " + streamResult.error().orElseThrow().message());
			}
			List<T> encodedOptions = streamResult.getOrThrow().toList();
			ArrayList<String> errors = Lists.newArrayListWithExpectedSize(encodedOptions.size());
			List<@Nullable Ingredient> ingredients = Lists.newArrayListWithExpectedSize(encodedOptions.size());
			for (T option : encodedOptions) {
				DataResult<Ingredient> result = Ingredient.CODEC.parse(ops, option);
				if (result.isSuccess()) {
					ingredients.add(result.getOrThrow());
					continue;
				}
				DataResult<Stream<T>> optionStream = ops.getStream(option);
				if (optionStream.isSuccess() && optionStream.getOrThrow().findAny().isEmpty()) {
					ingredients.add(null);
					continue;
				}
				errors.add(result.error().map(DataResult.Error::message).orElse("unknown decode error"));
			}
			if (ingredients.isEmpty()) {
				return DataResult.error(() -> "No valid ingredient found: " + String.join(", ", errors));
			}
			return DataResult.success(new AlternativesIngredient(ingredients));
		}

		@Override
		public <T> RecordBuilder<T> encode(AlternativesIngredient input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
			Stream<T> encoded;
			if (!input.options.isEmpty()) {
				encoded = input.options.stream().map(option -> option == null ? ops.emptyList() :
						Ingredient.CODEC.encodeStart(ops, option).getOrThrow());
			} else {
				encoded = input.wrapped == null ? Stream.of(ops.emptyList()) :
						Stream.of(Ingredient.CODEC.encodeStart(ops, input.wrapped).getOrThrow());
			}
			return prefix.add("options", ops.createList(encoded));
		}
	}
}
