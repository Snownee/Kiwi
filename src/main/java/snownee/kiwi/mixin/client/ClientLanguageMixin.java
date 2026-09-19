package snownee.kiwi.mixin.client;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.server.packs.resources.ResourceManager;
import snownee.kiwi.lang.TranslationPreprocessor;

@Mixin(ClientLanguage.class)
public class ClientLanguageMixin {
	@WrapOperation(
			method = "loadFrom",
			at = @At(value = "INVOKE", target = "Ljava/util/Map;copyOf(Ljava/util/Map;)Ljava/util/Map;"))
	private static Map<String, String> kiwi$preprocessTranslations(
			Map<String, String> map,
			Operation<Map<String, String>> original,
			@Local(argsOnly = true) ResourceManager resourceManager) {
		return original.call(TranslationPreprocessor.process(map, resourceManager));
	}
}
