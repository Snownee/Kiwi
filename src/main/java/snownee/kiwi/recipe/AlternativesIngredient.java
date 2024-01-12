package snownee.kiwi.recipe;

import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import snownee.kiwi.Kiwi;

public class AlternativesIngredient implements CustomIngredient {
	public static final ResourceLocation ID = new ResourceLocation(Kiwi.MODID, "alternatives");
	@Nullable
	private final JsonArray options;
	private Ingredient cached;

	public AlternativesIngredient(@Nullable JsonArray options) {
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
					ingredient = Ingredient.fromJson(option);
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

		@Override
		public ResourceLocation getIdentifier() {
			return ID;
		}

		@Override
		public AlternativesIngredient read(JsonObject json) {
			return new AlternativesIngredient(GsonHelper.getAsJsonArray(json, "options"));
		}

		@Override
		public void write(JsonObject json, AlternativesIngredient ingredient) {
			json.add("options", ingredient.options);
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
	}
}
