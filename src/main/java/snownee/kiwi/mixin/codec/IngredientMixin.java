package snownee.kiwi.mixin.codec;

import java.util.Arrays;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.fabricmc.fabric.impl.recipe.ingredient.CustomIngredientImpl;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.AnyIngredient;
import net.minecraft.world.item.crafting.Ingredient;
import snownee.kiwi.util.codec.IngredientCodecs;

@Mixin(Ingredient.class)
public class IngredientMixin {
	@Inject(method = "<clinit>", at = @At("RETURN"))
	private static void lychee$assignCodec(final CallbackInfo ci) {
		IngredientCodecs.NON_EMPTY_MAP_CODEC = IngredientCodecs.<CustomIngredientSerializer<?>, CustomIngredient, Ingredient.Value>dispatchMapOrElse(
						CustomIngredientImpl.CODEC,
						CustomIngredientImpl.TYPE_KEY,
						CustomIngredient::getSerializer,
						it -> it.getCodec(false),
						IngredientCodecs.VALUE_MAP_CODEC
				)
				.xmap(
						either -> either.map(
								CustomIngredient::toVanilla,
								it -> Ingredient.fromValues(Stream.of(it))
						), ingredient -> {
							if (ingredient.getCustomIngredient() != null) {
								return Either.left(ingredient.getCustomIngredient());
							} else {
								if (ingredient.values.length == 1) {
									return Either.right(ingredient.values[0]);
								}
								return Either.left(new AnyIngredient(Arrays.stream(ingredient.values)
										.map(it -> Ingredient.fromValues(Stream.of(it)))
										.toList()));
							}
						})
				.validate(ingredient -> {
					if (ingredient.getCustomIngredient() != null && ingredient.isEmpty()) {
						return DataResult.error(() -> "Cannot serialize empty ingredient using the map codec");
					}
					return DataResult.success(ingredient);
				});
	}
}