package snownee.kiwi.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.world.level.storage.LevelStorageSource;
import snownee.kiwi.KiwiClientConfig;

@Mixin(WorldOpenFlows.class)
public abstract class WorldOpenFlowsMixin {
	@ModifyVariable(method = "confirmWorldCreation", at = @At("HEAD"), argsOnly = true)
	private static boolean kiwi$confirmWorldCreation(boolean value) {
		if (KiwiClientConfig.suppressExperimentalWarning) {
			return true;
		}
		return value;
	}

	@Inject(method = "askForBackup", at = @At("HEAD"), cancellable = true)
	private void kiwi$askForBackup(
			LevelStorageSource.LevelStorageAccess levelStorageAccess,
			boolean isOldCustomizedWorld,
			Runnable runnable,
			Runnable runnable2,
			CallbackInfo ci) {
		if (KiwiClientConfig.suppressExperimentalWarning && !isOldCustomizedWorld) {
			runnable.run();
			ci.cancel();
		}
	}
}
