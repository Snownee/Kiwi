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
import net.neoforged.neoforge.common.extensions.IBlockStateExtension;
import net.neoforged.neoforge.common.util.TriState;
import snownee.kiwi.customization.CustomFeatureTags;

@Mixin(IBlockStateExtension.class)
public interface IBlockStateExtensionMixin {

	@Inject(method = "canSustainPlant", at = @At("HEAD"), cancellable = true)
	private void kiwi$canSustainPlant(
			BlockGetter level,
			BlockPos soilPosition,
			Direction facing,
			BlockState plant,
			CallbackInfoReturnable<TriState> cir) {
		BlockState state = (BlockState) this;
		if (state.isFaceSturdy(level, soilPosition, Direction.UP, SupportType.CENTER) && state.is(CustomFeatureTags.SUSTAIN_PLANT)) {
			cir.setReturnValue(TriState.TRUE);
		}
	}

}
