package snownee.kiwi.mixin.customization;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import snownee.kiwi.customization.CustomFeatureTags;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(BushBlock.class)
public abstract class BushBlockMixin {
	@Inject(method = "mayPlaceOn", at = @At("HEAD"), cancellable = true)
	private void kiwi$mayPlaceOn(BlockState pState, BlockGetter pLevel, BlockPos pPos, CallbackInfoReturnable<Boolean> cir) {
		if (pState.isFaceSturdy(pLevel, pPos, Direction.UP, SupportType.CENTER) && pState.is(CustomFeatureTags.SUSTAIN_PLANT)) {
			cir.setReturnValue(true);
		}
	}
}
