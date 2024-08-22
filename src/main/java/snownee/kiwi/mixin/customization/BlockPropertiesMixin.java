package snownee.kiwi.mixin.customization;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.level.block.state.BlockBehaviour;
import snownee.kiwi.customization.block.KBlockSettings;
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
}
