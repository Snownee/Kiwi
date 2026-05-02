package snownee.kiwi.mixin.customization;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.customization.CustomizationHooks;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {
	@WrapOperation(
			method = "lambda$stillValid$0",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z"
			))
	private static boolean kiwi$is(BlockState instance, Object block, Operation<Boolean> original) {
		boolean result = original.call(instance, block);
		if (result || !CustomizationHooks.isEnabled()) {
			return result;
		}
		return instance.getBlock().getClass() == block.getClass();
	}
}
