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
			method = "lambda$handleBlockEntityData$0",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/entity/BlockEntity;loadWithComponents(Lnet/minecraft/world/level/storage/ValueInput;)V"
			)
	)
	private void kiwi$handleBlockEntityData(BlockEntity blockEntity, ValueInput valueInput, Operation<Void> original) {
		if (blockEntity instanceof ModBlockEntity) {
			ClientPacketListener listener = (ClientPacketListener) (Object) this;
			((ModBlockEntity) blockEntity).onDataPacket(listener.getConnection(), valueInput);
		} else {
			original.call(blockEntity, valueInput);
		}
	}
}
