package snownee.kiwi.mixin.customization;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import snownee.kiwi.customization.block.KBlockSettings;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(Block.class)
public abstract class BlockMixin {
	@Inject(method = "propagatesSkylightDown", at = @At("HEAD"), cancellable = true)
	private void kiwi$propagatesSkylightDown(BlockState pState, BlockGetter pLevel, BlockPos pPos, CallbackInfoReturnable<Boolean> cir) {
		KBlockSettings settings = KBlockSettings.of(this);
		if (settings != null && settings.glassType != null) {
			cir.setReturnValue(true);
		}
	}
}
