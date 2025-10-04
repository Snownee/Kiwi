package snownee.kiwi.mixin.customization;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.block.state.properties.WoodType;
import snownee.kiwi.util.codec.WoodTypeCodec;

@Mixin(WoodType.class)
public class WoodTypeMixin {
	@Inject(method = "register", at = @At("TAIL"))
	private static void kiwi$register(final WoodType type, final CallbackInfoReturnable<WoodType> cir) {
		WoodTypeCodec.BY_NAME.put(type.name(), type);
	}
}
