package snownee.kiwi.mixin.customization.sit;

import java.util.Objects;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.world.entity.Entity;
import snownee.kiwi.customization.block.behavior.SitManager;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin extends ClientCommonPacketListenerImpl {
	protected ClientPacketListenerMixin(
			Minecraft p_295454_,
			Connection p_294773_,
			CommonListenerCookie p_294647_) {
		super(p_295454_, p_294773_, p_294647_);
	}

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
