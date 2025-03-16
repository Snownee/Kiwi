package snownee.kiwi.mixin.codec;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.datafixers.util.Either;

import net.minecraft.world.item.crafting.Ingredient;
import snownee.kiwi.util.codec.IngredientCodecs;

@Mixin(Ingredient.Value.class)
public interface Ingredient_ValueMixin {
	@Inject(method = "<clinit>", at = @At(value = "RETURN"))
	private static void lychee$assignCodec(final CallbackInfo ci) {
		IngredientCodecs.VALUE_MAP_CODEC =
				IngredientCodecs.xor(IngredientCodecs.ITEM_VALUE_MAP_CODEC, IngredientCodecs.TAG_VALUE_MAP_CODEC)
						.xmap(
								it -> it.map(Function.identity(), Function.identity()), it -> {
									if (it instanceof Ingredient.TagValue tagValue) {
										return Either.right(tagValue);
									} else if (it instanceof Ingredient.ItemValue itemValue) {
										return Either.left(itemValue);
									} else {
										throw new UnsupportedOperationException("This is neither an item value nor a tag value.");
									}
								});
	}
}