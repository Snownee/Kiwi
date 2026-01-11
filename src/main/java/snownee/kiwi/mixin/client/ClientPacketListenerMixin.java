package snownee.kiwi.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import snownee.kiwi.block.entity.ModBlockEntity;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

	@WrapOperation(
			method = {"method_38542", "lambda$handleBlockEntityData$5"},
			remap = false,
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/entity/BlockEntity;loadWithComponents(Lnet/minecraft/world/level/storage/ValueInput;)V",
					remap = true
			)
	)
	private void kiwi$handleBlockEntityData(
			final BlockEntity blockEntity, final ValueInput valueInput, final Operation<Void> original) {
		if (blockEntity instanceof ModBlockEntity) {
			ClientPacketListener listener = (ClientPacketListener) (Object) this;
			((ModBlockEntity) blockEntity).onDataPacket(listener.getConnection(), valueInput);
		} else {
			original.call(blockEntity, valueInput);
		}
	}
}
