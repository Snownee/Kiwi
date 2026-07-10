package snownee.kiwi.mixin.recipe;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import snownee.kiwi.recipe.KiwiRecipe;

@Mixin(CraftingRecipe.class)
public interface CraftingRecipeMixin {
	@Inject(method = "getRemainingItems", at = @At("HEAD"), cancellable = true)
	private void kiwi$getRemainingItems(CraftingInput input, CallbackInfoReturnable<NonNullList<ItemStack>> cir) {
		if (this instanceof KiwiRecipe kiwiRecipe && kiwiRecipe.kiwi$noRemainders()) {
			cir.setReturnValue(NonNullList.withSize(input.size(), ItemStack.EMPTY));
		}
	}
}
