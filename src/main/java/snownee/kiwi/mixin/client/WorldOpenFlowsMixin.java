package snownee.kiwi.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.world.level.storage.LevelStorageSource;
import snownee.kiwi.KiwiClientConfig;

@Mixin(WorldOpenFlows.class)
public abstract class WorldOpenFlowsMixin {

	@Inject(method = "confirmWorldCreation", at = @At("HEAD"), cancellable = true)
	private static void kiwi$confirmWorldCreation(Minecraft minecraft, CreateWorldScreen createWorldScreen, Lifecycle lifecycle, Runnable runnable, boolean bl, CallbackInfo ci) {
		if (KiwiClientConfig.suppressExperimentalWarning && lifecycle == Lifecycle.experimental()) {
			runnable.run();
			ci.cancel();
		}
	}

	@Inject(method = "loadLevel", at = @At("HEAD"), cancellable = true)
	private void kiwi$loadLevel(LevelStorageSource.LevelStorageAccess levelStorageAccess, Dynamic<?> dynamic, boolean bl, boolean bl2, Runnable runnable, CallbackInfo ci) {
		if (KiwiClientConfig.suppressExperimentalWarning && !bl) {
			loadLevel(levelStorageAccess, dynamic, true, bl2, runnable);
			ci.cancel();
		}
	}

	@Shadow
	protected abstract void loadLevel(LevelStorageSource.LevelStorageAccess levelStorageAccess, Dynamic<?> dynamic, boolean bl, boolean bl2, Runnable runnable);

}
