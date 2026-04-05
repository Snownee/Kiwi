package snownee.kiwi.mixin.customization;

import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.google.common.collect.Sets;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import snownee.kiwi.customization.CustomizationHooks;

@Mixin(BlockEntityType.class)
public abstract class BlockEntityTypeMixin {
	@Shadow
	@Final
	private Set<Block> validBlocks;

	@Unique
	private @Nullable Boolean kiwi$lenient;
	@Unique
	private @Nullable
	volatile Set<Block> kiwi$lenientValidBlocks;

	@Shadow
	@Nullable
	public abstract Holder.Reference<BlockEntityType<?>> builtInRegistryHolder();

	@SuppressWarnings("SuspiciousMethodCalls")
	@WrapOperation(method = "isValid", at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"))
	public boolean isValid(Set<Block> instance, Object object, Operation<Boolean> original) {
		if (!CustomizationHooks.isEnabled()) {
			return original.call(instance, object);
		}
		if (object == null) {
			return false;
		}
		if (kiwi$lenientValidBlocks != null && kiwi$lenientValidBlocks.contains(object)) {
			return true;
		}
		if (original.call(instance, object)) {
			return true;
		}
		if (kiwi$lenient == null) {
			Holder.Reference<BlockEntityType<?>> reference = builtInRegistryHolder();
			if (reference == null) {
				return false;
			}
			Identifier key = reference.key().identifier();
			kiwi$lenient = CustomizationHooks.getLenientBETypeNamespaces().contains(key.getNamespace());
		}
		if (kiwi$lenient == Boolean.FALSE) {
			return false;
		}
		for (Block validBlock : validBlocks) {
			if (validBlock.getClass() == object.getClass()) {
				if (kiwi$lenientValidBlocks == null) {
					//noinspection SynchronizeOnNonFinalField
					synchronized (validBlocks) {
						if (kiwi$lenientValidBlocks == null) {
							kiwi$lenientValidBlocks = Sets.newHashSet(validBlocks);
						}
					}
				}
				kiwi$lenientValidBlocks.add((Block) object);
				return true;
			}
		}
		return false;
	}

}
