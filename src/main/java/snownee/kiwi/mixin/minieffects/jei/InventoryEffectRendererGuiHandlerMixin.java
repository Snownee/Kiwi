package snownee.kiwi.mixin.minieffects.jei;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mezz.jei.library.plugins.vanilla.gui.InventoryEffectRendererGuiHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import snownee.kiwi.minieffects.EffectRenderingScreen;
import snownee.kiwi.minieffects.KiwiEffectsInInventory;

@Mixin(InventoryEffectRendererGuiHandler.class)
public class InventoryEffectRendererGuiHandlerMixin {
	@Inject(method = "getGuiExtraAreas", at = @At("HEAD"), cancellable = true)
	private void getGuiExtraAreas(AbstractContainerScreen<?> containerScreen, CallbackInfoReturnable<List<Rect2i>> cir) {
		if (containerScreen instanceof EffectRenderingScreen screen) {
			cir.setReturnValue(((KiwiEffectsInInventory) screen.kiwi$effects()).kiwi$areas().stream().map($ -> new Rect2i(
					$.left(),
					$.top(),
					$.width() - 1,
					$.height())).toList());
		}
	}
}
