package snownee.kiwi.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.kiwi.block.entity.BaseBlockEntity;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

	@Shadow
	Connection connection;

	@Inject(
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;load(Lnet/minecraft/nbt/CompoundTag;)V", remap = true
			), method = { "method_38542", "lambda$handleBlockEntityData$5" }, cancellable = true, remap = false
	)
	private void kiwi$handleBlockEntityData(ClientboundBlockEntityDataPacket clientboundBlockEntityDataPacket, BlockEntity blockEntity, CallbackInfo ci) {
		if (blockEntity instanceof BaseBlockEntity) {
			((BaseBlockEntity) blockEntity).onDataPacket(connection, clientboundBlockEntityDataPacket);
			ci.cancel();
		}
	}

}
