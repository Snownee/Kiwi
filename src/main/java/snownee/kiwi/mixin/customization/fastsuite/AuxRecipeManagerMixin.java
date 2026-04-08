package snownee.kiwi.mixin.customization.fastsuite;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Pseudo
@Mixin(targets = "dev.shadowsoffire.fastsuite.AuxRecipeManager")
public class AuxRecipeManagerMixin {

//	@ModifyReturnValue(method = "getRecipesFor", at = @At(value = "RETURN"))
//	private <C extends RecipeInput, T extends Recipe<C>> List<RecipeHolder<T>> kiwi$addFakeStonecutterRecipes(
//			List<RecipeHolder<T>> recipes,
//			RecipeType<T> pRecipeType,
//			C pInventory) {
//		if (pRecipeType == RecipeType.STONECUTTING) {
//			return StonecutterRecipeMaker.appendRecipesFor(recipes, pInventory);
//		}
//		return recipes;
//	}
//
//	@ModifyReturnValue(
//			method = "getRecipeFor",
//			at = @At("RETURN"))
//	private <C extends RecipeInput, T extends Recipe<C>> Optional<RecipeHolder<T>> kiwi$injectFakeStonecutterRecipe(
//			Optional<RecipeHolder<T>> original,
//			RecipeType<T> pRecipeType,
//			C pInventory,
//			Level level,
//			@Nullable RecipeHolder<T> pLastRecipe) {
//		if (pRecipeType == RecipeType.STONECUTTING && original.isEmpty()) {
//			return StonecutterRecipeMaker.appendRecipesFor(List.<RecipeHolder<T>>of(), pInventory).stream().findAny();
//		}
//		return original;
//	}
}
