package snownee.kiwi.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import snownee.kiwi.contributor.client.CosmeticRenderState;

@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private <T extends Avatar> void kiwi$extractRenderState(T entity, AvatarRenderState state, float partialTicks, CallbackInfo ci) {
		((CosmeticRenderState) state).kiwi$setName(entity.getProfile().name().orElse(null));
	}

}
