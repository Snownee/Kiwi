package snownee.kiwi.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends EffectRenderingInventoryScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
	@Shadow
	private float scrollOffs;

	@Shadow
	protected abstract boolean checkTabClicked(CreativeModeTab p_98563_, double p_98564_, double p_98565_);

	@Unique
	private static float persistentScrollOffs = 0;
	@Unique
	private CreativeModeTab clickedTab;

	public CreativeModeInventoryScreenMixin(
			CreativeModeInventoryScreen.ItemPickerMenu menu,
			Inventory inventory,
			Component component) {
		super(menu, inventory, component);
	}

	@Inject(method = "removed", at = @At("HEAD"))
	private void kiwi$saveScrollOffs(CallbackInfo ci) {
		persistentScrollOffs = this.scrollOffs;
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void kiwi$restoreScrollOffs(CallbackInfo ci) {
		this.scrollOffs = persistentScrollOffs;
		this.menu.scrollTo(this.scrollOffs);
	}

	//fix https://bugs.mojang.com/browse/MC-179165
	@Inject(method = "mouseClicked", at = @At("HEAD"))
	private void kiwi$mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (button == 0) {
			clickedTab = null;
			double x = mouseX - (double) this.leftPos;
			double y = mouseY - (double) this.topPos;
			for (CreativeModeTab tab : CreativeModeTabs.tabs()) {
				if (this.checkTabClicked(tab, x, y)) {
					clickedTab = tab;
				}
			}
		}
	}

	@Inject(
			method = "mouseReleased",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen;selectTab(Lnet/minecraft/world/item/CreativeModeTab;)V"),
			cancellable = true)
	private void kiwi$mouseReleased(
			double mouseX,
			double mouseY,
			int button,
			CallbackInfoReturnable<Boolean> ci,
			@Local CreativeModeTab tab) {
		if (clickedTab != tab) {
			ci.setReturnValue(true);
		}
	}
}