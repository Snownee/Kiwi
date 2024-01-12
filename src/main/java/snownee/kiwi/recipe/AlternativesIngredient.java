package snownee.kiwi.recipe;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import snownee.kiwi.Kiwi;

public class AlternativesIngredient extends AbstractIngredient {
	public static final ResourceLocation ID = new ResourceLocation(Kiwi.ID, "alternatives");
	public static final Serializer SERIALIZER = new Serializer();
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
	public ItemStack @NotNull [] getItems() {
		return internal().getItems();
	}

	@Override
	public boolean isSimple() {
		return internal().isSimple();
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
	public IIngredientSerializer<? extends Ingredient> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public @NotNull JsonElement toJson() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("type", Objects.requireNonNull(CraftingHelper.getID(SERIALIZER)).toString());
		jsonObject.add("options", options);
		return jsonObject;
	}

	public static class Serializer implements IIngredientSerializer<AlternativesIngredient> {
		@Override
		public @NotNull AlternativesIngredient parse(FriendlyByteBuf buf) {
			Ingredient internal = Ingredient.fromNetwork(buf);
			AlternativesIngredient ingredient = new AlternativesIngredient(null);
			ingredient.cached = internal;
			return ingredient;
		}

		@Override
		public AlternativesIngredient parse(JsonObject json) {
			return new AlternativesIngredient(GsonHelper.getAsJsonArray(json, "options"));
		}

		@Override
		public void write(FriendlyByteBuf buf, AlternativesIngredient ingredient) {
			ingredient.internal().toNetwork(buf);
		}
	}
}
