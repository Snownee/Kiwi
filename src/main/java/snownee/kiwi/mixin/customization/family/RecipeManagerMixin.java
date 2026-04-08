package snownee.kiwi.mixin.customization.family;

import java.util.Optional;
import java.util.stream.Stream;

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
			return StonecutterRecipeMaker.appendRecipesFor(Stream.<RecipeHolder<T>>empty(), pInventory).findAny();
		}
		return original;
	}

}
