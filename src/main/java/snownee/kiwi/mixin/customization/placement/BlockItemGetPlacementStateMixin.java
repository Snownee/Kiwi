package snownee.kiwi.mixin.customization.placement;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.Kiwi;
import snownee.kiwi.customization.block.KBlockSettings;
import snownee.kiwi.customization.placement.PlacementSystem;

@Mixin({BlockItem.class, StandingAndWallBlockItem.class})
public class BlockItemGetPlacementStateMixin {
	@WrapOperation(
			method = "getPlacementState",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/Block;getStateForPlacement(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;"))
	private BlockState kiwi$getPlacementState(Block block, BlockPlaceContext pContext, Operation<BlockState> original) {
		BlockState blockState = original.call(block, pContext);
		if (blockState == null || !blockState.is(block)) {
			return blockState;
		}
		KBlockSettings settings = KBlockSettings.of(block);
		if (settings != null) {
			blockState = settings.getStateForPlacement(blockState, pContext);
		}
		try {
			//noinspection DataFlowIssue
			BlockItem item = (BlockItem) (Object) this;
			blockState = PlacementSystem.onPlace(item, blockState, pContext);
		} catch (Throwable t) {
			Kiwi.LOGGER.error("Failed to handle placement for %s".formatted(blockState), t);
		}
		return blockState;
	}
}
