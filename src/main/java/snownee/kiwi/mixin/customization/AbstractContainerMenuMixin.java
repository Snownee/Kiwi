package snownee.kiwi.mixin.customization;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.customization.CustomizationHooks;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {
	@WrapOperation(
			method = {"lambda$stillValid$0", "m_38913_", "method_17696"},
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
	private static boolean is(
			BlockState instance,
			Block block,
			Operation<Boolean> original,
			@Local(argsOnly = true) Level level,
			@Local(argsOnly = true) BlockPos pos) {
		boolean result = original.call(instance, block);
		if (result || !CustomizationHooks.isEnabled()) {
			return result;
		}
		return instance.getBlock().getClass() == block.getClass();
	}
}
