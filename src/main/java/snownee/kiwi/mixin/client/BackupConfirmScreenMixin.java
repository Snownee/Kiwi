package snownee.kiwi.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screens.BackupConfirmScreen;
import snownee.kiwi.KiwiClientConfig;

@Mixin(BackupConfirmScreen.class)
public class BackupConfirmScreenMixin {
	@Shadow
	private boolean forceBackup;

	@Inject(method = "init", at = @At("HEAD"))
	private void kiwi$init(CallbackInfo ci) {
		if (KiwiClientConfig.noForceBackup) {
			forceBackup = false;
		}
	}
}
