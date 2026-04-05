package snownee.kiwi.mixin.client;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CreativeModeTab;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
	@Shadow
	private float scrollOffs;

	@Shadow
	protected abstract boolean checkTabClicked(CreativeModeTab tab, double xm, double ym);

	@Unique
	private static float kiwi$persistentScrollOffs = 0;
	@Unique
	private @Nullable CreativeModeTab kiwi$clickedTab;

	public CreativeModeInventoryScreenMixin(
			CreativeModeInventoryScreen.ItemPickerMenu menu,
			Inventory inventory,
			Component component) {
		super(menu, inventory, component);
	}

	@Inject(method = "removed", at = @At("HEAD"))
	private void kiwi$saveScrollOffs(CallbackInfo ci) {
		kiwi$persistentScrollOffs = this.scrollOffs;
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void kiwi$restoreScrollOffs(CallbackInfo ci) {
		this.scrollOffs = kiwi$persistentScrollOffs;
		this.menu.scrollTo(this.scrollOffs);
	}

	//fix https://bugs.mojang.com/browse/MC-179165
	@Inject(method = "mouseClicked", at = @At("HEAD"))
	private void kiwi$mouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
		if (event.button() == 0) {
			kiwi$clickedTab = null;
			double x = event.x() - (double) this.leftPos;
			double y = event.y() - (double) this.topPos;
			CreativeModeInventoryScreen self = (CreativeModeInventoryScreen) (Object) this;
			for (CreativeModeTab tab : self.getCurrentPage().getVisibleTabs()) {
				if (this.checkTabClicked(tab, x, y)) {
					kiwi$clickedTab = tab;
				}
			}
		}
	}

	@WrapOperation(
			method = "mouseReleased",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen;checkTabClicked(Lnet/minecraft/world/item/CreativeModeTab;DD)Z"))
	private boolean kiwi$mouseReleased(
			CreativeModeInventoryScreen instance,
			CreativeModeTab tab,
			double xm,
			double ym,
			Operation<Boolean> original) {
		return kiwi$clickedTab == tab && original.call(instance, tab, xm, ym);
	}
}