package snownee.kiwi.recipe;

import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import snownee.kiwi.Kiwi;

public class AlternativesIngredient implements CustomIngredient {
	public static final ResourceLocation ID = new ResourceLocation(Kiwi.ID, "alternatives");
	@Nullable
	private final List<JsonElement> options;
	private Ingredient cached;

	public AlternativesIngredient(@Nullable List<JsonElement> options) {
		this.options = options;
	}

	@Override
	public boolean test(ItemStack stack) {
		return internal().test(stack);
	}

	@Override
	public List<ItemStack> getMatchingStacks() {
		return List.of(internal().getItems());
	}

	@Override
	public boolean requiresTesting() {
		return internal().requiresTesting();
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

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return Serializer.INSTANCE;
	}

	public enum Serializer implements CustomIngredientSerializer<AlternativesIngredient> {
		INSTANCE;

		public static final Codec<AlternativesIngredient> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.list(ExtraCodecs.JSON).fieldOf("options").forGetter(o -> o.options)
		).apply(i, AlternativesIngredient::new));

		@Override
		public ResourceLocation getIdentifier() {
			return ID;
		}

		@Override
		public AlternativesIngredient read(FriendlyByteBuf buf) {
			Ingredient internal = Ingredient.fromNetwork(buf);
			AlternativesIngredient ingredient = new AlternativesIngredient(null);
			ingredient.cached = internal;
			return ingredient;
		}

		@Override
		public void write(FriendlyByteBuf buf, AlternativesIngredient ingredient) {
			ingredient.internal().toNetwork(buf);
		}

		@Override
		public Codec<AlternativesIngredient> getCodec(boolean allowEmpty) {
			return CODEC;
		}
	}
}
