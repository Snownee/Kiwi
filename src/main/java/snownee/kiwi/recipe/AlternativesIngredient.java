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
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import snownee.kiwi.Kiwi;

public class AlternativesIngredient implements CustomIngredient {
	public static final Identifier ID = Kiwi.id("alternatives");
	public final @Nullable Ingredient wrapped;

	public AlternativesIngredient(@Nullable Ingredient wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public boolean test(ItemStack stack) {
		return wrapped != null && wrapped.test(stack);
	}

	@SuppressWarnings("deprecation")
	@Override
	public Stream<Holder<Item>> items() {
		return wrapped != null ? wrapped.items() : Stream.empty();
	}

	@Override
	public boolean requiresTesting() {
		return wrapped != null && wrapped.requiresTesting();
	}

	@Override
	public SlotDisplay display() {
		return wrapped != null ? wrapped.display() : SlotDisplay.Empty.INSTANCE;
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return Serializer.INSTANCE;
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
		public MapCodec<AlternativesIngredient> getCodec() {
			return this;
		}

		public static AlternativesIngredient read(RegistryFriendlyByteBuf buf) {
			return new AlternativesIngredient(Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.decode(buf).orElse(null));
		}

		public static void write(RegistryFriendlyByteBuf buf, AlternativesIngredient ingredient) {
			Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.encode(buf, Optional.ofNullable(ingredient.wrapped));
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, AlternativesIngredient> getStreamCodec() {
			return STREAM_CODEC;
		}

		@Override
		public <T> Stream<T> keys(DynamicOps<T> ops) {
			return Stream.of(ops.createString("options"));
		}

		@Override
		public <T> DataResult<AlternativesIngredient> decode(DynamicOps<T> ops, MapLike<T> input) {
			List<T> options = ops.getStream(Objects.requireNonNull(input.get("options"))).getOrThrow().toList();
			ArrayList<String> errorMsgs = Lists.newArrayListWithExpectedSize(options.size());
			for (T option : options) {
				DataResult<Ingredient> result = Ingredient.CODEC.parse(ops, option);
				if (result.isSuccess()) {
					Ingredient ingredient = result.getOrThrow();
					if (ingredient.values.unwrapKey().isPresent() && ops instanceof RegistryOps<T> registryOps) {
						HolderGetter<Item> getter = registryOps.getter(Registries.ITEM).orElseThrow();
						Optional<HolderSet.Named<Item>> named = getter.get(ingredient.values.unwrapKey().get());
						if (named.isEmpty()) {
							continue;
						}
					}
					return DataResult.success(new AlternativesIngredient(ingredient));
				}
				DataResult<Stream<T>> stream = ops.getStream(option);
				if (stream.isSuccess() && stream.getOrThrow().toList().isEmpty()) {
					return DataResult.success(new AlternativesIngredient(null));
				}
				errorMsgs.add(result.error().orElseThrow().message());
			}
			return DataResult.error(() -> "No valid ingredient found: " + String.join(", ", errorMsgs));
		}

		@Override
		public <T> RecordBuilder<T> encode(AlternativesIngredient input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
			return prefix.add(
					"options",
					ops.createList(input.wrapped == null ?
							Stream.empty() :
							Stream.of(Ingredient.CODEC.encodeStart(ops, input.wrapped).getOrThrow())));
		}
	}
}
