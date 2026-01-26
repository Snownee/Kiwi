package snownee.kiwi.recipe;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import snownee.kiwi.Kiwi;

public class AlternativesIngredient implements CustomIngredient {
	public static final Identifier ID = Kiwi.id("alternatives");
	private final @Nullable List<JsonElement> options;
	private @Nullable Ingredient cached;

	public AlternativesIngredient(@Nullable List<JsonElement> options) {
		this.options = options;
	}

	@Override
	public boolean test(ItemStack stack) {
		internal();
		return cached != null && cached.test(stack);
	}

	@Override
	public Stream<Holder<Item>> items() {
		internal();
		//noinspection deprecation
		return cached != null ? cached.items() : Stream.empty();
	}

	@Override
	public boolean requiresTesting() {
		internal();
		return cached != null && cached.requiresTesting();
	}

	public Optional<Ingredient> internal() {
		if (cached == null && options != null) {
			for (JsonElement option : options) {
				Ingredient ingredient;
				try {
					ingredient = Ingredient.CODEC.parse(JsonOps.INSTANCE, option).result().orElseThrow();
				} catch (Exception e) {
					continue;
				}
				if (ingredient.isEmpty()) {
					continue;
				}
				cached = ingredient;
				break;
			}
		}
		return Optional.ofNullable(cached);
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return Serializer.INSTANCE;
	}

	public enum Serializer implements CustomIngredientSerializer<AlternativesIngredient> {
		INSTANCE;

		public static final MapCodec<AlternativesIngredient> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				Codec.list(ExtraCodecs.JSON).fieldOf("options").forGetter(o -> o.options)
		).apply(i, AlternativesIngredient::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, Optional<Ingredient>> INGREDIENT_STREAM_CODEC = Ingredient.CONTENTS_STREAM_CODEC.apply(
				ByteBufCodecs::optional);

		public static final StreamCodec<RegistryFriendlyByteBuf, AlternativesIngredient> STREAM_CODEC = StreamCodec.of(
				Serializer::write,
				Serializer::read);

		@Override
		public Identifier getIdentifier() {
			return ID;
		}

		@Override
		public MapCodec<AlternativesIngredient> getCodec() {
			return CODEC;
		}

		public static AlternativesIngredient read(RegistryFriendlyByteBuf buf) {
			Optional<Ingredient> internal = INGREDIENT_STREAM_CODEC.decode(buf);
			AlternativesIngredient ingredient = new AlternativesIngredient(null);
			ingredient.cached = internal.orElse(null);
			return ingredient;
		}

		public static void write(RegistryFriendlyByteBuf buf, AlternativesIngredient ingredient) {
			INGREDIENT_STREAM_CODEC.encode(buf, ingredient.internal());
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, AlternativesIngredient> getStreamCodec() {
			return STREAM_CODEC;
		}
	}
}
