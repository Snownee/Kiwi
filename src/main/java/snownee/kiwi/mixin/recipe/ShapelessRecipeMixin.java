package snownee.kiwi.mixin.recipe;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import snownee.kiwi.recipe.AlternativesIngredient;
import snownee.kiwi.recipe.CustomIngredientImpl;

@Mixin(ShapelessRecipe.class)
public class ShapelessRecipeMixin {
	@Mutable
	@Shadow
	@Final
	private List<Ingredient> ingredients;
	@Unique
	private boolean kiwi$trimmed;

	@Unique
	private static boolean kiwi$isEmptyAlternatives(Ingredient ingredient) {
		return ingredient.getCustomIngredient() instanceof CustomIngredientImpl<?> bridge
				&& bridge.ingredient() instanceof AlternativesIngredient
				&& ingredient.isEmpty();
	}

	@Inject(
			method = {
					"createPlacementInfo",
					"matches(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/world/level/Level;)Z",
					"display"}, at = @At("HEAD"))
	private void kiwi$trim(CallbackInfoReturnable<PlacementInfo> cir) {
		if (kiwi$trimmed) {
			return;
		}
		kiwi$trimmed = true;
		List<Ingredient> filtered = ingredients.stream().filter(ingredient -> !kiwi$isEmptyAlternatives(ingredient)).toList();
		if (filtered.size() != ingredients.size()) {
			ingredients = filtered;
		}
	}
}
