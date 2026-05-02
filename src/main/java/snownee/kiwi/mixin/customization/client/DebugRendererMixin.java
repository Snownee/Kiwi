package snownee.kiwi.mixin.customization.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import snownee.kiwi.customization.builder.BuildersButton;
import snownee.kiwi.customization.placement.PlaceDebugRenderer;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {
	@Inject(method = "emitGizmos", at = @At("TAIL"))
	private void kiwi$emitGizmos(
			Frustum frustum,
			double camX,
			double camY,
			double camZ,
			float partialTicks,
			CallbackInfo ci,
			@Local(name = "debugValues") DebugValueAccess debugValues) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}
		if ((player.isCreative() || player.isSpectator()) && player.getItemBySlot(EquipmentSlot.HEAD).is(Items.CHAINMAIL_HELMET)) {
			PlaceDebugRenderer.getInstance().emitGizmos(camX, camY, camZ, debugValues, frustum, partialTicks);
		}
		BuildersButton.getPreviewRenderer().emitGizmos(camX, camY, camZ, debugValues, frustum, partialTicks);
	}
}