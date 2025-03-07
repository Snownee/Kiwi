package snownee.kiwi.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.client.loading.NeoForgeLoadingOverlay;
import snownee.kiwi.KiwiClientConfig;

@Mixin(NeoForgeLoadingOverlay.class)
public class NeoForgeLoadingOverlayMixin {
	@Shadow
	private long fadeOutStart;

	@Inject(method = "render", at = @At("TAIL"))
	private void kiwi$render(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo ci) {
		if (KiwiClientConfig.loadingOverlayNoFade && fadeOutStart > 0L) {
			fadeOutStart = 0L;
		}
	}
}
