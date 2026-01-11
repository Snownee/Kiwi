package snownee.kiwi.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import snownee.kiwi.KiwiClientConfig;
import snownee.kiwi.client.TooltipEvents;
import snownee.kiwi.config.KiwiConfigManager;

@Mixin(Screen.class)
public class ScreenMixin {

	@Inject(method = "defaultHandleGameClickEvent", at = @At("HEAD"), cancellable = true)
	private static void kiwi$defaultHandleGameClickEvent(ClickEvent clickEvent, Minecraft mc, Screen screen, CallbackInfo ci) {
		if (clickEvent instanceof ClickEvent.Custom custom) {
			if (custom.id().equals(TooltipEvents.DISABLE_DEBUG_TOOLTIP)) {
				if (KiwiClientConfig.tagsTooltip) {
					KiwiClientConfig.tagsTooltip = false;
					KiwiConfigManager.getHandler(KiwiClientConfig.class).save();
				}
				LocalPlayer player = mc.player;
				if (player != null) {
					player.displayClientMessage(Component.translatable("tip.kiwi.debug_tooltip.success"), false);
				}
				ci.cancel();
			}
		}
	}

}
