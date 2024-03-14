package snownee.kiwi.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
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
}
