package snownee.kiwi.mixin.minieffects;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import snownee.kiwi.minieffects.EffectRenderingScreen;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin implements EffectRenderingScreen {
	@Shadow
	@Final
	private EffectsInInventory effects;

	@Override
	public EffectsInInventory kiwi$effects() {
		return effects;
	}
}
