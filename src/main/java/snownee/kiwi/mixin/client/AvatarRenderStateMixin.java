package snownee.kiwi.mixin.client;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import snownee.kiwi.contributor.CosmeticRenderState;
import snownee.kiwi.contributor.client.CosmeticLayer;

@Mixin(AvatarRenderState.class)
public class AvatarRenderStateMixin implements CosmeticRenderState {
	@Unique
	private @Nullable CosmeticLayer kiwi$cosmeticLayer;
	@Unique @Nullable String kiwi$name;

	@Override
	public @Nullable CosmeticLayer kiwi$getCosmeticLayer() {
		return kiwi$cosmeticLayer;
	}

	@Override
	public void kiwi$setCosmeticLayer(CosmeticLayer layer) {
		kiwi$cosmeticLayer = layer;
	}

	@Override
	public @Nullable String kiwi$getName() {
		return kiwi$name;
	}

	@Override
	public void kiwi$setName(String name) {
		kiwi$name = name;
	}
}
