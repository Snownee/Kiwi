package snownee.kiwi.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffects;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {

	@Inject(at = @At("HEAD"), method = "renderFire", cancellable = true)
	private static void kiwi$renderFire(Minecraft minecraft, PoseStack poseStack, CallbackInfo ci) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}
		if (player.isCreative()) {
			ci.cancel();
		}
		if (player.isEyeInFluid(FluidTags.LAVA) && (player.fireImmune() || player.hasEffect(MobEffects.FIRE_RESISTANCE))) {
			poseStack.translate(0, -0.25, 0);
		}
	}

}