package snownee.kiwi.recipe;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.CompoundIngredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.common.crafting.conditions.ConditionContext;
import net.minecraftforge.common.crafting.conditions.ICondition;
import snownee.kiwi.Kiwi;
import snownee.kiwi.mixin.RecipeManagerAccess;
import snownee.kiwi.util.Util;

public class AlternativesIngredient extends AbstractIngredient {
	public static final ResourceLocation ID = new ResourceLocation(Kiwi.ID, "alternatives");
	public static final Serializer SERIALIZER = new Serializer();
	@Nullable
	private JsonArray options;
	private Ingredient cached;

	public AlternativesIngredient(@Nullable JsonArray options) {
		this.options = options;
		internal(); // force load due to the networking bug
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
		return false;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	public Ingredient internal() {
		if (cached == null) {
			Objects.requireNonNull(options);
			cached = Ingredient.EMPTY;
			for (JsonElement option : options) {
				try {
					cached = getIngredient(option);
					if (!cached.isEmpty()) {
						options = null;
						break;
					}
				} catch (IllegalStateException e) {
					Kiwi.LOGGER.info("Failed to parse ingredient: %s".formatted(options), e);
					cached = null;
					return Ingredient.EMPTY;
				} catch (Exception ignored) {
				}
			}
		}
		return cached;
	}

	private Ingredient getIngredient(JsonElement element) {
		if (element.isJsonObject()) {
			JsonObject jsonObject = element.getAsJsonObject();
			if (jsonObject.size() == 1 && jsonObject.has("tag")) {
				RecipeManager recipeManager = Util.getRecipeManager();
				if (recipeManager == null) {
					throw new IllegalStateException("Unable to get recipe manager");
				}
				ICondition.IContext ctx = ((RecipeManagerAccess) recipeManager).getContext();
				if (!(ctx instanceof ConditionContext)) {
					throw new IllegalStateException("Unable to get real condition context");
				}
				String s = jsonObject.get("tag").getAsString();
				TagKey<Item> tagKey = TagKey.create(Registries.ITEM, new ResourceLocation(s));
				if (ctx.getTag(tagKey).isEmpty()) {
					throw new JsonSyntaxException("Tag not found: " + s);
				}
			}
			return CraftingHelper.getIngredient(element, false);
		} else if (element.isJsonArray()) {
			JsonArray jsonArray = element.getAsJsonArray();
			if (jsonArray.isEmpty()) {
				throw new JsonSyntaxException("Empty array");
			}
			Ingredient[] ingredients = new Ingredient[jsonArray.size()];
			for (int i = 0; i < jsonArray.size(); i++) {
				ingredients[i] = getIngredient(jsonArray.get(i));
			}
			return CompoundIngredient.of(ingredients);
		}
		throw new JsonSyntaxException("Expected item to be object or array of objects");
	}

	@Override
	public IIngredientSerializer<? extends Ingredient> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public @NotNull JsonElement toJson() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("type", Objects.requireNonNull(CraftingHelper.getID(SERIALIZER)).toString());
		jsonObject.add("options", Objects.requireNonNull(options));
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
