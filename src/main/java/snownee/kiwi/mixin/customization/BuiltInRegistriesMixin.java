package snownee.kiwi.mixin.customization;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.registries.BuiltInRegistries;
import snownee.kiwi.customization.CustomizationHooks;

@Mixin(BuiltInRegistries.class)
public class BuiltInRegistriesMixin {
	@Inject(method = "bootStrap", at = @At("RETURN"))
	private static void onBootstrap(CallbackInfo ci) {
		CustomizationHooks.frozen();
	}
}
