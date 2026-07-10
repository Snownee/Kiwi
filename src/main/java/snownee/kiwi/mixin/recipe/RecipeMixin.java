package snownee.kiwi.mixin.recipe;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.world.item.crafting.Recipe;
import snownee.kiwi.recipe.KiwiRecipe;
import snownee.kiwi.util.codec.ExtendedCodec;

@Mixin(value = Recipe.class, priority = 2000)
public interface RecipeMixin {
	@WrapOperation(
			method = "<clinit>", at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/serialization/Codec;dispatch(Ljava/util/function/Function;Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"))
	private static <E, A> Codec<E> kiwi$wrapCodec(
			Codec<E> instance,
			Function<? super E, ? extends A> type,
			Function<? super A, ? extends MapCodec<? extends E>> codec,
			Operation<Codec<E>> original) {
		Codec<E> result = original.call(instance, type, codec);
		return new ExtendedCodec<>(
				result,
				Codec.BOOL.optionalFieldOf("kiwi:no_remainders", false).codec(),
				(recipe, noRemainders) -> {
					if (recipe instanceof KiwiRecipe kiwiRecipe) {
						kiwiRecipe.kiwi$setNoRemainders(noRemainders);
					}
				},
				recipe -> recipe instanceof KiwiRecipe kiwiRecipe && kiwiRecipe.kiwi$noRemainders());
	}
}
