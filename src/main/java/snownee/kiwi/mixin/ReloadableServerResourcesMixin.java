package snownee.kiwi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.item.crafting.RecipeManager;
import snownee.kiwi.util.Util;

@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesMixin {

	@Shadow
	private RecipeManager recipes;

	@Inject(at = @At("TAIL"), method = "<init>*")
	private void kiwi_init(CallbackInfo ci) {
		Util.recipeManager = recipes;
	}

}
