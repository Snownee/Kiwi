package snownee.kiwi.mixin.client;

import com.mojang.blaze3d.platform.InputConstants;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.options.MouseSettingsScreen;

@Mixin(OptionInstance.class)
public class OptionInstanceMixin {
	@Shadow
	private Object value;

	@Inject(at = @At("HEAD"), method = "get", cancellable = true)
	private void kiwi$get(CallbackInfoReturnable<Object> ci) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.screen instanceof MouseSettingsScreen) {
			return;
		}
		//noinspection ConstantValue
		if (mc.options != null && this == (Object) mc.options.mouseWheelSensitivity() && kiwi$isControlDown(mc)) {
			ci.setReturnValue((Double) value * 4);
		}
	}

	@Unique
	private static boolean kiwi$isControlDown(Minecraft mc) {
		var window = mc.getWindow();
		return InputConstants.isKeyDown(window, 341)
				|| InputConstants.isKeyDown(window, 345)
				|| InputConstants.isKeyDown(window, 343)
				|| InputConstants.isKeyDown(window, 347);
	}
}