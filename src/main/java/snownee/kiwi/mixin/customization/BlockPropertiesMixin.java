package snownee.kiwi.mixin.customization;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Codec;

import net.minecraft.world.level.block.state.BlockBehaviour;
import snownee.kiwi.customization.block.KBlockSettings;
import snownee.kiwi.customization.block.loader.BuiltInBlockTemplate;
import snownee.kiwi.customization.block.loader.InjectedCodec;
import snownee.kiwi.customization.duck.KBlockProperties;

@Mixin(BlockBehaviour.Properties.class)
public class BlockPropertiesMixin implements KBlockProperties {
	@Unique
	private KBlockSettings settings;

	@Override
	public @Nullable KBlockSettings kiwi$getSettings() {
		return settings;
	}

	@Override
	public void kiwi$setSettings(KBlockSettings settings) {
		this.settings = settings;
	}

	@WrapOperation(
			method = "<clinit>",
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/serialization/Codec;unit(Ljava/util/function/Supplier;)Lcom/mojang/serialization/Codec;"))
	private static Codec<BlockBehaviour.Properties> kiwi$injectCodec(
			Supplier<BlockBehaviour.Properties> defaultValue,
			Operation<Codec<BlockBehaviour.Properties>> original) {
		return new InjectedCodec<>(original.call(defaultValue), BuiltInBlockTemplate.PROPERTIES_INJECTOR);
	}
}
