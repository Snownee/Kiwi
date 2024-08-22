package snownee.kiwi.mixin.customization.property_inject;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.customization.block.KBlockSettings;

@Mixin(Block.class)
public class BlockMixin {
	@Shadow
	private BlockState defaultBlockState;

	@Inject(method = "registerDefaultState", at = @At("RETURN"))
	private void kiwi$registerDefaultState(BlockState pState, CallbackInfo ci) {
		KBlockSettings settings = KBlockSettings.of(this);
		if (settings != null) {
			defaultBlockState = settings.registerDefaultState(defaultBlockState);
		}
	}
}
