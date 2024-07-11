package snownee.kiwi.mixin.customization;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.kiwi.Kiwi;
import snownee.kiwi.customization.block.KBlockSettings;
import snownee.kiwi.customization.placement.PlacementSystem;
import snownee.kiwi.customization.shape.BlockShapeType;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateMixin {
	@Shadow
	public abstract Block getBlock();

	@Shadow
	protected abstract BlockState asState();

	@Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
	private void kiwi$canSurvive(LevelReader pLevel, BlockPos pPos, CallbackInfoReturnable<Boolean> cir) {
		KBlockSettings settings = KBlockSettings.of(getBlock());
		if (settings != null && settings.canSurviveHandler != null) {
			cir.setReturnValue(settings.canSurviveHandler.canSurvive(asState(), pLevel, pPos));
		}
	}

	@Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
	private void kiwi$checkCanSurvive(
			Direction pDirection,
			BlockState pNeighborState,
			LevelAccessor pLevel,
			BlockPos pPos,
			BlockPos pNeighborPos,
			CallbackInfoReturnable<BlockState> cir) {
		KBlockSettings settings = KBlockSettings.of(getBlock());
		if (settings != null && settings.canSurviveHandler != null && settings.canSurviveHandler.isSensitiveSide(asState(), pDirection) &&
				!settings.canSurviveHandler.canSurvive(asState(), pLevel, pPos)) {
			cir.setReturnValue(Blocks.AIR.defaultBlockState());
		}
	}

	@Inject(method = "updateShape", at = @At("RETURN"), cancellable = true)
	private void kiwi$updateShape(
			Direction pDirection,
			BlockState pNeighborState,
			LevelAccessor pLevel,
			BlockPos pPos,
			BlockPos pNeighborPos,
			CallbackInfoReturnable<BlockState> cir) {
		if (!cir.getReturnValue().is(getBlock())) {
			return;
		}
		KBlockSettings settings = KBlockSettings.of(getBlock());
		if (settings != null) {
			cir.setReturnValue(settings.updateShape(cir.getReturnValue(), pDirection, pNeighborState, pLevel, pPos, pNeighborPos));
		}
	}

	@Inject(method = "canBeReplaced(Lnet/minecraft/world/item/context/BlockPlaceContext;)Z", at = @At("HEAD"), cancellable = true)
	private void kiwi$canBeReplaced(BlockPlaceContext pUseContext, CallbackInfoReturnable<Boolean> cir) {
		KBlockSettings settings = KBlockSettings.of(getBlock());
		if (settings == null) {
			return;
		}
		Boolean triState = settings.canBeReplaced(asState(), pUseContext);
		if (triState != null) {
			cir.setReturnValue(triState);
		}
	}

	@WrapOperation(
			method = "onRemove",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/Block;onRemove(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V"))
	private void kiwi$onRemove(
			Block block,
			BlockState oldState,
			Level level,
			BlockPos pos,
			BlockState newState,
			boolean pMovedByPiston,
			Operation<Void> original) {
		original.call(block, oldState, level, pos, newState, pMovedByPiston);
		try {
			PlacementSystem.onBlockRemoved(level, pos, oldState, newState);
		} catch (Throwable t) {
			Kiwi.LOGGER.error("Failed to handle placement for %s".formatted(oldState), t);
		}
	}

	@WrapOperation(
			method = "getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/Block;getShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;"))
	private VoxelShape kiwi$getShape(
			Block instance,
			BlockState blockState,
			BlockGetter blockGetter,
			BlockPos pos,
			CollisionContext context,
			Operation<VoxelShape> original) {
		KBlockSettings settings = KBlockSettings.of(getBlock());
		if (settings != null && settings.getShape(BlockShapeType.MAIN) != null) {
			try {
				return settings.getShape(BlockShapeType.MAIN).getShape(blockState, context);
			} catch (Exception ignored) {
			}
		}
		return original.call(instance, blockState, blockGetter, pos, context);
	}

	@WrapOperation(
			method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/Block;getCollisionShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;"))
	private VoxelShape kiwi$getCollisionShape(
			Block instance,
			BlockState blockState,
			BlockGetter blockGetter,
			BlockPos pos,
			CollisionContext context,
			Operation<VoxelShape> original) {
		KBlockSettings settings = KBlockSettings.of(getBlock());
		if (settings != null && settings.getShape(BlockShapeType.COLLISION) != null) {
			try {
				return settings.getShape(BlockShapeType.COLLISION).getShape(blockState, context);
			} catch (Exception ignored) {
			}
		}
		return original.call(instance, blockState, blockGetter, pos, context);
	}

	@WrapOperation(
			method = "getInteractionShape", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/Block;getInteractionShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;"))
	private VoxelShape kiwi$getInteractionShape(
			Block instance,
			BlockState blockState,
			BlockGetter blockGetter,
			BlockPos pos,
			Operation<VoxelShape> original) {
		KBlockSettings settings = KBlockSettings.of(getBlock());
		if (settings != null && settings.getShape(BlockShapeType.INTERACTION) != null) {
			try {
				return settings.getShape(BlockShapeType.INTERACTION).getShape(blockState, CollisionContext.empty());
			} catch (Exception ignored) {
			}
		}
		return original.call(instance, blockState, blockGetter, pos);
	}
}
