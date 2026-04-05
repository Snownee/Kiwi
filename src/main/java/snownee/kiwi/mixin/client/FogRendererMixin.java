package snownee.kiwi.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FogType;

@Mixin(FogRenderer.class)
public class FogRendererMixin {

	@WrapOperation(
			method = "setupFog",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/fog/environment/FogEnvironment;isApplicable(Lnet/minecraft/world/level/material/FogType;Lnet/minecraft/world/entity/Entity;)Z"))
	private static boolean kiwi$setupFog(FogEnvironment instance, FogType fogType, Entity entity, Operation<Boolean> original) {
		if (entity instanceof Player player) {
			if (player.isCreative() || player.fireImmune() || player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
				return true;
			}
		}
		return original.call(instance, fogType, entity);
	}

}