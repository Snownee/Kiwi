package snownee.kiwi.recipe;

import java.util.Objects;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public abstract class KiwiRecipeSerializer<T extends Recipe<?>> implements RecipeSerializer<T> {
	private final Supplier<Codec<T>> codec = Suppliers.memoize(() -> ExtraCodecs.JSON.flatXmap((jsonElement) -> {
		try {
			return DataResult.success(fromJson(jsonElement.getAsJsonObject()));
		} catch (JsonParseException var3) {
			Objects.requireNonNull(var3);
			return DataResult.error(var3::getMessage);
		}
	}, (object) -> {
		try {
			JsonObject json = new JsonObject();
			toJson(json, object);
			return DataResult.success(json);
		} catch (IllegalArgumentException var3) {
			Objects.requireNonNull(var3);
			return DataResult.error(var3::getMessage);
		}
	}));

	@Override
	public Codec<T> codec() {
		return codec.get();
	}

	public abstract T fromJson(JsonObject json);

	public abstract void toJson(JsonObject json, T recipe);
}
