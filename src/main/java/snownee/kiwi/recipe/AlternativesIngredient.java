package snownee.kiwi.recipe;

import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import snownee.kiwi.Kiwi;

public class AlternativesIngredient implements CustomIngredient {
	public static final Identifier ID = Kiwi.id("alternatives");
	public static final Serializer SERIALIZER = new Serializer();
	@Nullable
	private final List<JsonElement> options;
	private Ingredient cached;

	public AlternativesIngredient(@Nullable List<JsonElement> options) {
		this.options = options;
	}

	@Override
	public boolean test(ItemStack stack) {
		internal();
		return cached != null && cached.test(stack);
	}

	@Override
	public List<ItemStack> getMatchingStacks() {
		internal();
		return cached != null ? List.of(cached.getItems()) : List.of();
	}

	@Override
	public boolean requiresTesting() {
		return true;
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return SERIALIZER;
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
				if (ingredient.getItems().length == 0) {
					continue;
				}
				cached = ingredient;
				break;
			}
		}
		return Optional.ofNullable(cached);
	}

	public static final class Serializer implements CustomIngredientSerializer<AlternativesIngredient> {
		public static final MapCodec<AlternativesIngredient> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				Codec.list(ExtraCodecs.JSON).fieldOf("options").forGetter(o -> o.options)
		).apply(i, AlternativesIngredient::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, Optional<Ingredient>> INGREDIENT_STREAM_CODEC =
				Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs::optional);

		public static final StreamCodec<RegistryFriendlyByteBuf, AlternativesIngredient> STREAM_CODEC = StreamCodec.of(
				Serializer::write,
				Serializer::read);

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
		public Identifier getIdentifier() {
			return ID;
		}

		@Override
		public MapCodec<AlternativesIngredient> getCodec(boolean allowEmpty) {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, AlternativesIngredient> getPacketCodec() {
			return STREAM_CODEC;
		}
	}
}
