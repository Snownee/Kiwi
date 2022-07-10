package snownee.kiwi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import snownee.kiwi.KiwiClientConfig;
import snownee.kiwi.client.TooltipEvents;
import snownee.kiwi.config.KiwiConfigManager;

@Mixin(Screen.class)
public class ScreenMixin {

	@Inject(
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;insertText(Ljava/lang/String;Z)V"
			), method = "handleComponentClicked", cancellable = true
	)
	private void kiwi_handleComponentClicked(Style style, CallbackInfoReturnable<Boolean> ci) {
		if (style.getClickEvent() == TooltipEvents.disableClickEvent) {
			if (KiwiClientConfig.tagsTooltip || KiwiClientConfig.nbtTooltip) {
				KiwiClientConfig.tagsTooltip = false;
				KiwiClientConfig.nbtTooltip = false;
				KiwiConfigManager.getHandler(KiwiClientConfig.class).save();
			}
			LocalPlayer player = Minecraft.getInstance().player;
			if (player != null) {
				player.sendSystemMessage(Component.translatable("tip.kiwi.debug_tooltip.success"));
			}
			ci.setReturnValue(true);
		}
	}

}
