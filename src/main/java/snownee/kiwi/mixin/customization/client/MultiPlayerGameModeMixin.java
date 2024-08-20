package snownee.kiwi.mixin.customization.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import snownee.kiwi.customization.builder.BuildersButton;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
	@Inject(
			method = "startDestroyBlock",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameType;isCreative()Z"),
			cancellable = true)
	private void kiwi$startDestroyBlock(BlockPos pos, Direction pFace, CallbackInfoReturnable<Boolean> cir) {
		if (BuildersButton.startDestroyBlock(pos, pFace)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "continueDestroyBlock", at = @At("HEAD"), cancellable = true)
	private void kiwi$continueDestroyBlock(BlockPos pPosBlock, Direction pDirectionFacing, CallbackInfoReturnable<Boolean> cir) {
		if (BuildersButton.startDestroyBlock(pPosBlock, pDirectionFacing)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "useItemOn", at = @At(value = "HEAD"), cancellable = true)
	private void kiwi$useItemOn(
			LocalPlayer pPlayer,
			InteractionHand pHand,
			BlockHitResult pResult,
			CallbackInfoReturnable<InteractionResult> cir) {
		if (BuildersButton.performUseItemOn(pHand, pResult)) {
			cir.setReturnValue(InteractionResult.CONSUME);
		}
	}
}
