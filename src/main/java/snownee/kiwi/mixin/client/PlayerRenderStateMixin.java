package snownee.kiwi.mixin.client;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import snownee.kiwi.contributor.CosmeticRenderState;
import snownee.kiwi.contributor.client.CosmeticLayer;

@Mixin(PlayerRenderState.class)
public class PlayerRenderStateMixin implements CosmeticRenderState {
	private CosmeticLayer kiwi$cosmeticLayer;

	@Override
	public CosmeticLayer kiwi$getCosmeticLayer() {
		return kiwi$cosmeticLayer;
	}

	@Override
	public void kiwi$setCosmeticLayer(CosmeticLayer layer) {
		kiwi$cosmeticLayer = layer;
	}
}
