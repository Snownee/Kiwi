package snownee.kiwi.mixin.customization.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import snownee.kiwi.customization.builder.BuildersButton;
import snownee.kiwi.customization.placement.PlaceDebugRenderer;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {
	@Inject(method = "render", at = @At("HEAD"))
	private void kiwi$render(
			PoseStack pPoseStack,
			MultiBufferSource.BufferSource pBufferSource,
			double pCamX,
			double pCamY,
			double pCamZ,
			CallbackInfo ci) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}
		if ((player.isCreative() || player.isSpectator()) && player.getItemBySlot(EquipmentSlot.HEAD).is(Items.CHAINMAIL_HELMET)) {
			PlaceDebugRenderer.getInstance().render(pPoseStack, pBufferSource, pCamX, pCamY, pCamZ);
		}
		BuildersButton.getPreviewRenderer().render(pPoseStack, pBufferSource, pCamX, pCamY, pCamZ);
	}
}
