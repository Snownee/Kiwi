package snownee.kiwi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraftforge.registries.GameData;
import snownee.kiwi.Kiwi;

@Mixin(value = GameData.class, remap = false)
public class GameDataMixin {

	@Inject(at = @At("TAIL"), method = "unfreezeData")
	private static void kiwi_unfreezeData(CallbackInfo ci) {
		Kiwi.preInit();
	}

}
