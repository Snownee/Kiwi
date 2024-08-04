/*
package snownee.kiwi.mixin.customization.forge;

import java.util.List;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.GameData;
import snownee.kiwi.customization.CustomizationHooks;
import snownee.kiwi.customization.CustomizationRegistries;

@Mixin(value = GameData.class, remap = false)
public class GameDataMixin {
	@Inject(method = "postRegisterEvents", at = @At(value = "INVOKE", target = "Ljava/lang/RuntimeException;<init>()V"))
	private static void kiwi$postRegisterEvents(CallbackInfo ci, @Local(ordinal = 1) Set<ResourceLocation> ordered) {
		if (!CustomizationHooks.isEnabled()) {
			return;
		}
		List<ResourceLocation> copy = List.copyOf(ordered);
		ordered.clear();
		List<ResourceLocation> prioritized = List.of(
				CustomizationRegistries.BLOCK_COMPONENT_KEY.location(),
				CustomizationRegistries.BLOCK_TEMPLATE_KEY.location(),
				CustomizationRegistries.ITEM_TEMPLATE_KEY.location());
		ordered.addAll(prioritized);
		ordered.addAll(copy.stream().filter($ -> !prioritized.contains($)).toList());
	}
}
*/
