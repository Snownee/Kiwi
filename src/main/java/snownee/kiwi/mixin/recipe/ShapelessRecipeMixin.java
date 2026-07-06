package snownee.kiwi.mixin.recipe;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import snownee.kiwi.recipe.AlternativesIngredient;

@Mixin(ShapelessRecipe.class)
public class ShapelessRecipeMixin {
	@Mutable
	@Shadow
	@Final
	private List<Ingredient> ingredients;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void kiwi$init(
			Recipe.CommonInfo commonInfo,
			CraftingRecipe.CraftingBookInfo bookInfo,
			ItemStackTemplate result,
			List<Ingredient> ingredients,
			CallbackInfo ci) {
		var filtered = this.ingredients.stream()
				.filter(ingredient -> !(ingredient.getCustomIngredient() instanceof AlternativesIngredient $ && $.wrapped == null))
				.toList();
		if (filtered.size() != this.ingredients.size()) {
			this.ingredients = filtered;
		}
	}
}
