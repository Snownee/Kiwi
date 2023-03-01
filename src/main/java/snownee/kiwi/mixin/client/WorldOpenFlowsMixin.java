package snownee.kiwi.mixin.client;

import com.mojang.serialization.Lifecycle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import snownee.kiwi.KiwiClientConfig;

@Mixin(WorldOpenFlows.class)
public class WorldOpenFlowsMixin {

	@Inject(method = "confirmWorldCreation", at = @At("HEAD"), cancellable = true)
	private static void kiwi$confirmWorldCreation(Minecraft minecraft, CreateWorldScreen createWorldScreen, Lifecycle lifecycle, Runnable runnable, CallbackInfo ci) {
		if (KiwiClientConfig.suppressExperimentalWarning && lifecycle == Lifecycle.experimental()) {
			runnable.run();
			ci.cancel();
		}
	}

}
