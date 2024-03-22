package snownee.kiwi.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.screens.TitleScreen;
import snownee.kiwi.KiwiClientConfig;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
	@Shadow
	private boolean fading;

	@Inject(method = "<init>(ZLnet/minecraft/client/gui/components/LogoRenderer;)V", at = @At("RETURN"))
	private void kiwi$init(boolean bl, LogoRenderer logoRenderer, CallbackInfo ci) {
		if (KiwiClientConfig.titleScreenNoFade) {
			fading = false;
		}
	}
}
