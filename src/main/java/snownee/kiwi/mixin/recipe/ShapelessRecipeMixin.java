package snownee.kiwi.mixin.recipe;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import snownee.kiwi.recipe.AlternativesIngredient;

@Mixin(ShapelessRecipe.class)
public class ShapelessRecipeMixin {
	@Mutable
	@Shadow
	@Final
	private List<Ingredient> ingredients;

	@Inject(
			method = {
					"createPlacementInfo",
					"matches(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/world/level/Level;)Z",
					"display"}, at = @At("HEAD"))
	private void kiwi$trim(CallbackInfoReturnable<PlacementInfo> cir) {
		var filtered = this.ingredients.stream()
				.filter(ingredient -> !(ingredient.getCustomIngredient() instanceof AlternativesIngredient && ingredient.isEmpty()))
				.toList();
		if (filtered.size() != this.ingredients.size()) {
			this.ingredients = filtered;
		}
	}
}
