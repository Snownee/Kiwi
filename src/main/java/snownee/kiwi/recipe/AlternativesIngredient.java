package snownee.kiwi.recipe;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

public class AlternativesIngredient implements ICustomIngredient {
	public static final IngredientType<AlternativesIngredient> SERIALIZER = new IngredientType<>(Serializer.CODEC, Serializer.STREAM_CODEC);
	@Nullable
	private List<JsonElement> options;
	private Ingredient cached;

	public AlternativesIngredient(@Nullable List<JsonElement> options) {
		this.options = options;
	}

	@Override
	public boolean test(ItemStack stack) {
		return internal().test(stack);
	}

	@Override
	public Stream<ItemStack> getItems() {
		return Stream.of(internal().getItems());
	}

	@Override
	public boolean isSimple() {
		return false;
	}

	@Override
	public IngredientType<?> getType() {
		return SERIALIZER;
	}

	public Ingredient internal() {
		if (cached == null) {
			Objects.requireNonNull(options);
			cached = Ingredient.EMPTY;
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
		return cached;
	}

	public static final class Serializer {
		public static final MapCodec<AlternativesIngredient> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				Codec.list(ExtraCodecs.JSON).fieldOf("options").forGetter(o -> o.options)
		).apply(i, AlternativesIngredient::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, AlternativesIngredient> STREAM_CODEC = StreamCodec.of(
				Serializer::write,
				Serializer::read);

		public static AlternativesIngredient read(RegistryFriendlyByteBuf buf) {
			Ingredient internal = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
			AlternativesIngredient ingredient = new AlternativesIngredient(null);
			ingredient.cached = internal;
			return ingredient;
		}

		public static void write(RegistryFriendlyByteBuf buf, AlternativesIngredient ingredient) {
			Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient.internal());
		}
	}
}
