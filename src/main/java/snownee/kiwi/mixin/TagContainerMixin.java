package snownee.kiwi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.tags.TagContainer;
import snownee.kiwi.Kiwi;

@Mixin(TagContainer.class)
public class TagContainerMixin {

	@Inject(method = "bindToGlobal", at = @At("RETURN"))
	private void kiwi_bindToGlobal(CallbackInfo ci) {
		Kiwi.onTagsUpdated();
	}

}
