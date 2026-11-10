package snownee.kiwi.mixin.customization.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import snownee.kiwi.customization.builder.ConvertScreen;

@Mixin(Gui.class)
public class GuiMixin {
	@Inject(
			method = "extractRenderState",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;applyCursor(Lcom/mojang/blaze3d/platform/Window;)V"))
	private void kiwi$renderLingeringScreen(
			DeltaTracker deltaTracker,
			boolean shouldRenderLevel,
			boolean resourcesLoaded,
			CallbackInfo ci,
			@Local(type = GuiGraphicsExtractor.class) GuiGraphicsExtractor graphics) {
		ConvertScreen.extractLingering(graphics);
	}
}
