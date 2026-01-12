package snownee.kiwi.mixin.customization.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import snownee.kiwi.customization.builder.BuildersButton;
import snownee.kiwi.customization.placement.PlaceDebugRenderer;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {
	@Inject(method = "render", at = @At("HEAD"))
	private void kiwi$render(
			PoseStack poseStack,
			Frustum frustum,
			MultiBufferSource.BufferSource bufferSource,
			double camX,
			double camY,
			double camZ,
			CallbackInfo ci) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}
		if ((player.isCreative() || player.isSpectator()) && player.getItemBySlot(EquipmentSlot.HEAD).is(Items.CHAINMAIL_HELMET)) {
			PlaceDebugRenderer.getInstance().render(poseStack, bufferSource, camX, camY, camZ);
		}
		BuildersButton.getPreviewRenderer().render(poseStack, bufferSource, camX, camY, camZ);
	}
}