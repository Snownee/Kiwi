package snownee.kiwi.mixin.customization;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import snownee.kiwi.customization.CustomizationHooks;
import snownee.kiwi.customization.block.KBlockSettings;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin {
	@Shadow
	public BlockBehaviour.Properties properties;

	@Inject(method = "getShadeBrightness", at = @At("HEAD"), cancellable = true)
	private void kiwi$getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
		KBlockSettings settings = KBlockSettings.of(this);
		if (settings != null && settings.glassType != null) {
			cir.setReturnValue(settings.glassType.shadeBrightness());
		}
	}

	@Inject(method = "skipRendering", at = @At("HEAD"), cancellable = true)
	private void kiwi$skipRendering(
			BlockState state,
			BlockState neighborState,
			Direction direction,
			CallbackInfoReturnable<Boolean> cir) {
		KBlockSettings settings = KBlockSettings.of(this);
		if (settings != null && settings.glassType != null && CustomizationHooks.skipGlassRendering(state, neighborState, direction)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "getVisualShape", at = @At("HEAD"), cancellable = true)
	private void kiwi$getVisualShape(
			BlockState state,
			BlockGetter level,
			BlockPos pos,
			CollisionContext context,
			CallbackInfoReturnable<VoxelShape> cir) {
		KBlockSettings settings = KBlockSettings.of(this);
		if (settings != null && settings.glassType != null) {
			cir.setReturnValue(Shapes.empty());
		}
	}

	@Inject(method = "getFluidState", at = @At("HEAD"), cancellable = true)
	private void kiwi$getFluidState(BlockState state, CallbackInfoReturnable<FluidState> cir) {
		KBlockSettings settings = KBlockSettings.of(this);
		if (settings != null && state.hasProperty(BlockStateProperties.WATERLOGGED)) {
			cir.setReturnValue(state.getValue(BlockStateProperties.WATERLOGGED) ?
					Fluids.WATER.getSource(false) :
					Fluids.EMPTY.defaultFluidState());
		}
	}

	@Inject(method = "rotate", at = @At("HEAD"), cancellable = true)
	private void kiwi$rotate(BlockState state, Rotation rotation, CallbackInfoReturnable<BlockState> cir) {
		KBlockSettings settings = KBlockSettings.of(this);
		if (settings != null) {
			cir.setReturnValue(settings.rotate(state, rotation));
		}
	}

	@Inject(method = "mirror", at = @At("HEAD"), cancellable = true)
	private void kiwi$mirror(BlockState state, Mirror mirror, CallbackInfoReturnable<BlockState> cir) {
		KBlockSettings settings = KBlockSettings.of(this);
		if (settings != null) {
			cir.setReturnValue(settings.mirror(state, mirror));
		}
	}

	@Inject(method = "useShapeForLightOcclusion", at = @At("HEAD"), cancellable = true)
	private void kiwi$useShapeForLightOcclusion(BlockState state, CallbackInfoReturnable<Boolean> cir) {
		KBlockSettings settings = KBlockSettings.of(this);
		if (settings != null) {
			cir.setReturnValue(settings.useShapeForLightOcclusion(state));
		}
	}

	@Inject(method = "hasAnalogOutputSignal", at = @At("HEAD"), cancellable = true)
	private void kiwi$hasAnalogOutputSignal(BlockState state, CallbackInfoReturnable<Boolean> cir) {
		KBlockSettings settings = KBlockSettings.of(this);
		if (settings != null && settings.analogOutputSignal != null) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "getAnalogOutputSignal", at = @At("HEAD"), cancellable = true)
	private void kiwi$getAnalogOutputSignal(
			BlockState state,
			Level level,
			BlockPos pos,
			Direction direction,
			CallbackInfoReturnable<Integer> cir) {
		KBlockSettings settings = KBlockSettings.of(this);
		if (settings != null && settings.analogOutputSignal != null) {
			cir.setReturnValue(settings.analogOutputSignal.applyAsInt(state));
		}
	}
}
