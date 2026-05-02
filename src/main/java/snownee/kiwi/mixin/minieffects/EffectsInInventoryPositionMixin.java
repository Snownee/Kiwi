package snownee.kiwi.mixin.minieffects;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import snownee.kiwi.minieffects.MiniEffects;

// trying to avoid conflicts with EffectsLeft (https://www.curseforge.com/minecraft/mc-mods/effectsleft)
@Mixin(value = EffectsInInventory.class, priority = 1100)
public class EffectsInInventoryPositionMixin {
	@Shadow
	@Final
	private AbstractContainerScreen<?> screen;

	@Inject(at = @At("HEAD"), method = "canSeeEffects", cancellable = true)
	private void kiwi$canSeeEffects(CallbackInfoReturnable<Boolean> ci) {
		if (MiniEffects.isLeftSide()) {
			ci.setReturnValue(screen.leftPos >= 34);
		}
	}

	@Definition(id = "xo", local = @Local(type = int.class, name = "xo"))
	@Expression("xo = @(?)")
	@ModifyExpressionValue(method = "extractRenderState", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
	private int kiwi$modifyX0(int xo) {
		if (!MiniEffects.isLeftSide()) {
			return xo;
		}
		// always use compact mode if is left side
		return screen.leftPos - 32;
	}

	@Definition(id = "availableWidth", local = @Local(type = int.class, name = "availableWidth"))
	@Expression("availableWidth = @(?)")
	@ModifyExpressionValue(method = "extractRenderState", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
	private int kiwi$modifyAvailableWidth(int availableWidth) {
		if (!MiniEffects.isLeftSide()) {
			return availableWidth;
		}
		// always use compact mode if is left side
		return screen.leftPos - 2;
	}

	@Definition(id = "maxWidth", local = @Local(type = int.class, name = "maxWidth"))
	@Expression("maxWidth")
	@ModifyExpressionValue(method = "extractRenderState", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
	private int kiwi$renderEffectsBl(int maxWidth, @Local(name = "availableWidth") int availableWidth) {
		if (MiniEffects.isLeftSide()) {
			// always use compact mode if is left side
			return 32;
		} else {
			// prevent full width mode from being modified by JEI
			return availableWidth >= 120 ? availableWidth - 7 : 32;
		}
	}
}