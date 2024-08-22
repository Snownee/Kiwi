package snownee.kiwi.mixin.customization.family;

import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import javax.annotation.Nullable;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import snownee.kiwi.customization.block.family.StonecutterRecipeMaker;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Mixin(RecipeManager.class)
public class RecipeManagerMixin {

	@ModifyReturnValue(method = "getRecipesFor", at = @At(value = "RETURN"))
	private <C extends RecipeInput, T extends Recipe<C>> List<RecipeHolder<T>> kiwi$addFakeStonecutterRecipes(
			List<RecipeHolder<T>> recipes,
			RecipeType<T> pRecipeType,
			C pInventory) {
		if (pRecipeType == RecipeType.STONECUTTING) {
			return StonecutterRecipeMaker.appendRecipesFor(recipes, pInventory);
		}
		return recipes;
	}

	@ModifyReturnValue(
			method = "getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/crafting/RecipeHolder;)Ljava/util/Optional;",
			at = @At("RETURN"))
	private <C extends RecipeInput, T extends Recipe<C>> Optional<RecipeHolder<T>> kiwi$injectFakeStonecutterRecipe(
			Optional<RecipeHolder<T>> original,
			RecipeType<T> pRecipeType,
			C pInventory,
			Level level,
			@Nullable RecipeHolder<T> pLastRecipe) {
		if (pRecipeType == RecipeType.STONECUTTING && original.isEmpty()) {
			return StonecutterRecipeMaker.appendRecipesFor(List.<RecipeHolder<T>>of(), pInventory).stream().findAny();
		}
		return original;
	}

}
