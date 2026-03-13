package snownee.kiwi.mixin.customization.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.GameRenderer;
import snownee.kiwi.customization.builder.ConvertScreen;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Inject(
			method = "extractGui",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/Minecraft;getOverlay()Lnet/minecraft/client/gui/screens/Overlay;",
					ordinal = 0))
	private void kiwi$renderLingeringScreen(
			DeltaTracker deltaTracker,
			boolean shouldRenderLevel,
			boolean resourcesLoaded,
			CallbackInfo ci,
			@Local(name = "graphics") GuiGraphicsExtractor graphics) {
		ConvertScreen.extractLingering(graphics);
	}
}
