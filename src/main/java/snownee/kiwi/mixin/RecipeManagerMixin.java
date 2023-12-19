package snownee.kiwi.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.gson.JsonElement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import snownee.kiwi.util.Util;

@Mixin(value = RecipeManager.class, priority = 9)
public class RecipeManagerMixin {

	@Inject(at = @At("HEAD"), method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V")
	private void kiwi$apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler, CallbackInfo ci) {
		Util.setRecipeManager((RecipeManager) (Object) this);
	}

	@Inject(at = @At("HEAD"), method = "replaceRecipes")
	private void kiwi$replaceRecipes(Iterable<Recipe<?>> pRecipes, CallbackInfo ci) {
		Util.setRecipeManager((RecipeManager) (Object) this);
	}

}
