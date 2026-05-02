package snownee.kiwi.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.renderer.fog.environment.LavaFogEnvironment;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

@Mixin(LavaFogEnvironment.class)
public class LavaFogEnvironmentMixin {

	@WrapOperation(
			method = "setupFog",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isSpectator()Z", ordinal = 0))
	private boolean kiwi$setupFog(Entity entity, Operation<Boolean> original) {
		if (entity instanceof Player player && player.isCreative()) {
			return true;
		}
		if (entity instanceof LivingEntity living) {
			if (living.fireImmune() || living.hasEffect(MobEffects.FIRE_RESISTANCE)) {
				return true;
			}
		}
		return original.call(entity);
	}

}