package snownee.kiwi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.telemetry.ClientTelemetryManager;
import net.minecraft.client.telemetry.TelemetryEventSender;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiClientConfig;

@Mixin(value = ClientTelemetryManager.class, priority = -114514)
public class ClientTelemetryManagerMixin {

	@Inject(method = "createWorldSessionEventSender", at = @At("HEAD"), cancellable = true)
	private void kiwi_createWorldSessionEventSender(CallbackInfoReturnable<TelemetryEventSender> ci) {
		if (KiwiClientConfig.noMicrosoftTelemetry) {
			Kiwi.logger.info("Kiwi: Canceling Microsoft telemetry");
			ci.setReturnValue(TelemetryEventSender.DISABLED);
		}
	}

}
