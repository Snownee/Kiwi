package snownee.kiwi.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffects;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {

	@WrapMethod(method = "renderFire")
	private static void kiwi$renderFire(
			PoseStack poseStack,
			MultiBufferSource bufferSource,
			TextureAtlasSprite sprite,
			Operation<Void> original) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			original.call(poseStack, bufferSource, sprite);
			return;
		}
		if (player.isEyeInFluid(FluidTags.LAVA) && (player.fireImmune() || player.hasEffect(MobEffects.FIRE_RESISTANCE))) {
			poseStack.translate(0, -0.25, 0);
		}
		if (!player.isCreative()) {
			original.call(poseStack, bufferSource, sprite);
		}
	}
}