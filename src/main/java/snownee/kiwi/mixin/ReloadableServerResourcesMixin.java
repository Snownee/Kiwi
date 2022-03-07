package snownee.kiwi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import snownee.kiwi.Kiwi;

@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesMixin {

	@Inject(method = "updateRegistryTags", at = @At("RETURN"))
	private void kiwi_updateRegistryTags(RegistryAccess registryAccess, CallbackInfo ci) {
		Kiwi.onTagsUpdated();
	}

}
