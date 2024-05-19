package snownee.kiwi.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

@Mixin(FogRenderer.class)
public class FogRendererMixin {

	@WrapOperation(
			method = "setupFog",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isSpectator()Z", ordinal = 0))
	private static boolean kiwi$setupFog(Entity entity, Operation<Boolean> original) {
		if (entity instanceof Player player) {
			if (player.isCreative() || player.fireImmune() || player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
				return true;
			}
		}
		return original.call(entity);
	}

}