package snownee.kiwi.mixin.client;

import java.util.Optional;
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.neoforged.neoforge.client.loading.NeoForgeLoadingOverlay;
import snownee.kiwi.KiwiClientConfig;

@Mixin(NeoForgeLoadingOverlay.class)
public abstract class NeoForgeLoadingOverlayMixin extends LoadingOverlay {

	private NeoForgeLoadingOverlayMixin(
			Minecraft minecraft,
			ReloadInstance reload,
			Consumer<Optional<Throwable>> onFinish,
			boolean fadeIn) {
		super(minecraft, reload, onFinish, fadeIn);
	}

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void kiwi$render(GuiGraphicsExtractor guiGraphics, int i, int j, float f, CallbackInfo ci) {
		if (KiwiClientConfig.loadingOverlayNoFade && fadeOutStart > 0L) {
			fadeOutStart = 0L;
		}
	}
}
