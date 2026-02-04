package snownee.kiwi.mixin.minieffects;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import snownee.kiwi.minieffects.EffectRenderingScreen;

@Mixin(CreativeModeInventoryScreen.class)
public class CreativeModeInventoryScreenMixin implements EffectRenderingScreen {
	@Shadow
	@Final
	private EffectsInInventory effects;

	@Override
	public EffectsInInventory kiwi$effects() {
		return effects;
	}
}
