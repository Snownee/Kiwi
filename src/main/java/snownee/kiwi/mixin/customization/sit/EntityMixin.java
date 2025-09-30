package snownee.kiwi.mixin.customization.sit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import snownee.kiwi.customization.block.behavior.SitManager;

@Mixin(Entity.class)
public class EntityMixin {
	@Inject(method = "getDismountLocationForPassenger", at = @At("HEAD"), cancellable = true)
	private void kiwi$getDismountLocationForPassenger(LivingEntity pPassenger, CallbackInfoReturnable<Vec3> cir) {
		Entity self = (Entity) (Object) this;
		if (SitManager.isSeatEntity(self)) {
			cir.setReturnValue(SitManager.dismount(self, pPassenger));
		}
	}
}
