package snownee.kiwi.mixin.customization.family;

import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import snownee.kiwi.customization.block.family.StonecutterRecipeMaker;

@Mixin(RecipeMap.class)
public class RecipeMapMixin {
	@ModifyReturnValue(method = "getRecipesFor", at = @At("RETURN"))
	private <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeHolder<T>> kiwi$addFakeStonecutterRecipes(
			Stream<RecipeHolder<T>> original,
			RecipeType<T> type,
			I container,
			Level level) {
		if (type == RecipeType.STONECUTTING) {
			StonecutterRecipeMaker.appendRecipesFor(original, container);
		}
		return original;
	}
}
