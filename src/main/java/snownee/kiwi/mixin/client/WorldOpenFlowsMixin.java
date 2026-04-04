package snownee.kiwi.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.serialization.Lifecycle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.world.level.storage.LevelStorageSource;
import snownee.kiwi.KiwiClientConfig;

@Mixin(WorldOpenFlows.class)
public abstract class WorldOpenFlowsMixin {
	@WrapMethod(method = "confirmWorldCreation")
	private static void kiwi$confirmWorldCreation(
			Minecraft minecraft,
			CreateWorldScreen parent,
			Lifecycle lifecycle,
			Runnable task,
			boolean skipWarning,
			Operation<Void> original) {
		original.call(minecraft, parent, lifecycle, task, KiwiClientConfig.suppressExperimentalWarning || skipWarning);
	}

	@Inject(method = "askForBackup", at = @At("HEAD"), cancellable = true)
	private void kiwi$askForBackup(
			LevelStorageSource.LevelStorageAccess worldAccess,
			boolean oldCustomized,
			Runnable proceedCallback,
			Runnable cancelCallback,
			CallbackInfo ci) {
		if (KiwiClientConfig.suppressExperimentalWarning && !oldCustomized) {
			proceedCallback.run();
			ci.cancel();
		}
	}
}
