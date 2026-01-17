package snownee.kiwi.mixin.client;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
	@Shadow
	private float scrollOffs;

	@Shadow
	protected abstract boolean checkTabClicked(CreativeModeTab p_98563_, double p_98564_, double p_98565_);

	@Unique
	private static float persistentScrollOffs = 0;
	@Unique
	private @Nullable CreativeModeTab clickedTab;

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
	private void kiwi$mouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
		if (event.button() == 0) {
			clickedTab = null;
			double x = event.x() - (double) this.leftPos;
			double y = event.y() - (double) this.topPos;
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
	private void kiwi$mouseReleased(MouseButtonEvent event, CallbackInfoReturnable<Boolean> ci, @Local CreativeModeTab tab) {
		if (clickedTab != tab) {
			ci.setReturnValue(true);
		}
	}
}