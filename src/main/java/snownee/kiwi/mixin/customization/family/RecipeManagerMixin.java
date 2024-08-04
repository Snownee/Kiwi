package snownee.kiwi.mixin.customization.family;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.datafixers.util.Pair;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.Level;
import snownee.kiwi.customization.block.family.StonecutterRecipeMaker;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Mixin(RecipeManager.class)
public class RecipeManagerMixin {

	@ModifyReturnValue(method = "getRecipesFor", at = @At(value = "RETURN"))
	private <C extends Container, T extends Recipe<C>> List<T> kiwi$addFakeStonecutterRecipes(
			List<T> recipes,
			RecipeType<T> pRecipeType,
			C pInventory) {
		if (pRecipeType == RecipeType.STONECUTTING) {
			//noinspection unchecked
			return (List<T>) StonecutterRecipeMaker.appendRecipesFor((List<StonecutterRecipe>) recipes, pInventory);
		}
		return recipes;
	}

	@ModifyReturnValue(
			method = "getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;Lnet/minecraft/resources/ResourceLocation;)Ljava/util/Optional;",
			at = @At("RETURN"))
	private <C extends Container, T extends Recipe<C>> Optional<Pair<ResourceLocation, T>> kiwi$injectFakeStonecutterRecipe(
			Optional<Pair<ResourceLocation, T>> original,
			RecipeType<T> pRecipeType,
			C pInventory,
			Level level,
			@Nullable ResourceLocation pLastRecipe) {
		if (pRecipeType == RecipeType.STONECUTTING && original.isEmpty()) {
			//noinspection unchecked
			return StonecutterRecipeMaker.appendRecipesFor(List.of(), pInventory).stream().findAny().map(recipe -> Pair.of(
					recipe.getId(),
					(T) recipe));
		}
		return original;
	}

	@ModifyReturnValue(
			method = "getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;",
			at = @At("RETURN"))
	private <C extends Container, T extends Recipe<C>> Optional<T> kiwi$injectFakeStonecutterRecipe(
			Optional<T> original,
			RecipeType<T> pRecipeType,
			C pInventory,
			Level level) {
		if (pRecipeType == RecipeType.STONECUTTING && original.isEmpty()) {
			//noinspection unchecked
			return (Optional<T>) StonecutterRecipeMaker.appendRecipesFor(List.of(), pInventory).stream().findAny();
		}
		return original;
	}

}
