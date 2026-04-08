package snownee.kiwi.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import snownee.kiwi.KiwiClientConfig;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(
			method = "handleDebugKeys",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;clearMessages(Z)V"))
	private void kiwi$handleDebugKeys(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (KiwiClientConfig.superClearChat) {
			minecraft.gui.clearTitles();
			minecraft.gui.resetTitleTimes();
			minecraft.getToastManager().clear();
			minecraft.getSoundManager().stop();
		}
	}
}
