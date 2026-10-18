package snownee.kiwi.mixin.client;

import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.effect.MobEffects;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {

	@WrapMethod(method = "submitFire")
	private static void kiwi$submitFire(
			PoseStack poseStack,
			SubmitNodeCollector bufferSource,
			TextureAtlasSprite sprite,
			Operation<Void> original) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			original.call(poseStack, bufferSource, sprite);
			return;
		}
		if (player.isCreative()) {
			return;
		}
		if (player.fireImmune() || player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
			poseStack.pushPose();
			poseStack.translate(0, -0.25, 0);
			original.call(poseStack, bufferSource, sprite);
			poseStack.popPose();
		}
	}

}