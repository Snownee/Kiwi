package snownee.kiwi.mixin.customization.family;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.google.common.collect.Iterables;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import snownee.kiwi.customization.block.family.StonecutterRecipeMaker;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
	@WrapOperation(
			method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Lnet/minecraft/world/item/crafting/RecipeMap;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/crafting/RecipeMap;create(Ljava/lang/Iterable;)Lnet/minecraft/world/item/crafting/RecipeMap;"))
	private RecipeMap kiwi$addStonecutterRecipes(
			Iterable<RecipeHolder<?>> recipes,
			Operation<RecipeMap> original) {
		return original.call(Iterables.concat(recipes, StonecutterRecipeMaker.makeRecipes()));
	}
}
