package snownee.kiwi.mixin.customization.forge;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import snownee.kiwi.customization.CustomFeatureTags;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;

@Mixin(Block.class)
public abstract class BlockMixin {
	@Inject(method = "canSustainPlant", at = @At("HEAD"), cancellable = true, remap = false)
	private void kiwi$canSustainPlant(
			BlockState state,
			BlockGetter world,
			BlockPos pos,
			Direction facing,
			IPlantable plantable,
			CallbackInfoReturnable<Boolean> cir) {
		PlantType type = plantable.getPlantType(world, pos.relative(facing));
		if (type == PlantType.PLAINS && state.isFaceSturdy(world, pos, Direction.UP, SupportType.CENTER) &&
				state.is(CustomFeatureTags.SUSTAIN_PLANT)) {
			cir.setReturnValue(true);
		}
	}
}
