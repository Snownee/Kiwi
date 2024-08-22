package snownee.kiwi.mixin.customization;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.customization.CustomizationHooks;
import snownee.kiwi.customization.block.KBlockSettings;

@Mixin(HalfTransparentBlock.class)
public abstract class HalfTransparentBlockMixin {
	@Inject(method = "skipRendering", at = @At("HEAD"), cancellable = true)
	private void kiwi$skipRendering(
			BlockState pState,
			BlockState pAdjacentBlockState,
			Direction pSide,
			CallbackInfoReturnable<Boolean> cir) {
		KBlockSettings settings = KBlockSettings.of(this);
		if (settings != null && settings.glassType != null) {
			// generally, XKDeco blocks should not extend HalfTransparentBlock. avoids stack overflow here.
			cir.setReturnValue(CustomizationHooks.skipGlassRendering(pState, pAdjacentBlockState, pSide));
		}
		settings = KBlockSettings.of(pAdjacentBlockState.getBlock());
		if (settings != null && settings.glassType != null && pAdjacentBlockState.skipRendering(pState, pSide.getOpposite())) {
			cir.setReturnValue(true);
		}
	}
}
