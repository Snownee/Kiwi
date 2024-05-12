package snownee.kiwi.mixin.sit;

import java.util.Objects;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.world.entity.Entity;
import snownee.kiwi.customization.block.behavior.SitManager;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(
			method = "handleSetEntityPassengersPacket",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/Gui;setOverlayMessage(Lnet/minecraft/network/chat/Component;Z)V"))
	private void kiwi$setPlayerYRotOnSeat(ClientboundSetPassengersPacket pPacket, CallbackInfo ci, @Local(ordinal = 0) Entity vehicle) {
		if (SitManager.isSeatEntity(vehicle)) {
			Objects.requireNonNull(minecraft.player);
			minecraft.player.yRotO = vehicle.getYRot();
			minecraft.player.setYRot(vehicle.getYRot());
			minecraft.player.setYHeadRot(vehicle.getYRot());
		}
	}
}
