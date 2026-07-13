package snownee.kiwi.mixin.customization;

import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.google.common.collect.Sets;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

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
	private volatile @Nullable Set<Block> kiwi$lenientValidBlocks;

	@Shadow
	public abstract Holder.@Nullable Reference<BlockEntityType<?>> builtInRegistryHolder();

	@SuppressWarnings("SuspiciousMethodCalls")
	@WrapOperation(method = "isValid", at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"))
	public boolean isValid(Set<Block> instance, @Nullable Object object, Operation<Boolean> original) {
		if (!CustomizationHooks.isEnabled()) {
			return original.call(instance, object);
		}
		if (object == null) {
			return false;
		}
		Set<Block> lenientValidBlocks;
		//noinspection SynchronizeOnNonFinalField
		synchronized (validBlocks) {
			lenientValidBlocks = kiwi$lenientValidBlocks;
			if (lenientValidBlocks != null && lenientValidBlocks.contains(object)) {
				return true;
			}
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
		if (!kiwi$lenient) {
			return false;
		}
		for (Block validBlock : validBlocks) {
			if (validBlock.getClass() == object.getClass()) {
				//noinspection SynchronizeOnNonFinalField
				synchronized (validBlocks) {
					lenientValidBlocks = kiwi$lenientValidBlocks;
					if (lenientValidBlocks == null) {
						lenientValidBlocks = kiwi$lenientValidBlocks = Sets.newHashSet(validBlocks);
					}
					lenientValidBlocks.add((Block) object);
				}
				return true;
			}
		}
		return false;
	}

}
