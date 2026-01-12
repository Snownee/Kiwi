package snownee.kiwi.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import snownee.kiwi.contributor.CosmeticRenderState;
import snownee.kiwi.contributor.client.CosmeticLayer;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
	@ModifyReturnValue(
			method = "createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;",
			at = @At("RETURN"))
	private EntityRenderState kiwi$appendState(final EntityRenderState state, Entity player) {
		if (!((Object) this instanceof AvatarRenderer)) {
			return state;
		}
		((CosmeticRenderState) state).kiwi$setCosmeticLayer(CosmeticLayer.getRendererOf((AbstractClientPlayer) player));
		return state;
	}
}
