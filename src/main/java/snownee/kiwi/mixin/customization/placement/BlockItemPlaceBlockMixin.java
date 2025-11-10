package snownee.kiwi.mixin.customization.placement;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.Kiwi;
import snownee.kiwi.customization.placement.PlacementSystem;

@Mixin(BlockItem.class)
public class BlockItemPlaceBlockMixin {
	@Inject(method = "placeBlock", at = @At("TAIL"))
	private void kiwi$placeBlock(BlockPlaceContext pContext, BlockState pState, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValueZ()) {
			try {
				PlacementSystem.onBlockPlaced(pContext);
			} catch (Throwable t) {
				Kiwi.LOGGER.error("Failed to handle placement for %s".formatted(pState), t);
			}
		}
	}
}
