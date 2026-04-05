package snownee.kiwi.mixin.client;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import snownee.kiwi.contributor.client.CosmeticRenderState;

@Mixin(AvatarRenderState.class)
public class AvatarRenderStateMixin implements CosmeticRenderState {

	@Unique
	private @Nullable String kiwi$name;

	@Override
	public @Nullable String kiwi$getName() {
		return kiwi$name;
	}

	@Override
	public void kiwi$setName(@Nullable String name) {
		kiwi$name = name;
	}

}
