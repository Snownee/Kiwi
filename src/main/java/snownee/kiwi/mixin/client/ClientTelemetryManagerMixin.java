package snownee.kiwi.mixin.client;

import net.minecraft.SharedConstants;
import net.minecraft.client.ClientTelemetryManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiClientConfig;

@Mixin(value = ClientTelemetryManager.class, priority = -114514)
public class ClientTelemetryManagerMixin {

	@Redirect(
			method = "<init>", at = @At(
			value = "FIELD", target = "Lnet/minecraft/SharedConstants;IS_RUNNING_IN_IDE:Z"
	), require = 0
	)
	private boolean getIS_RUNNING_IN_IDE() {
		if (KiwiClientConfig.noMicrosoftTelemetry) {
			Kiwi.logger.info("Canceling Microsoft telemetry");
		}
		return KiwiClientConfig.noMicrosoftTelemetry || SharedConstants.IS_RUNNING_IN_IDE;
	}

}
