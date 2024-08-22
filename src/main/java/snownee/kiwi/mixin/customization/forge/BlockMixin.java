package snownee.kiwi.mixin.customization.forge;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import snownee.kiwi.customization.CustomFeatureTags;

@Mixin(IBlockExtension.class)
public abstract class BlockMixin {

	@Inject(method = "canSustainPlant", at = @At("HEAD"), cancellable = true)
	private void kiwi$canSustainPlant(
			BlockState state,
			BlockGetter level,
			BlockPos soilPosition,
			Direction facing,
			BlockState plant,
			CallbackInfoReturnable<Boolean> cir) {
		if (state.isFaceSturdy(level, soilPosition, Direction.UP, SupportType.CENTER) && state.is(CustomFeatureTags.SUSTAIN_PLANT)) {
			cir.setReturnValue(true);
		}
	}

}
