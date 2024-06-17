package snownee.kiwi.block;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.block.BlockPickInteractionAware;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import snownee.kiwi.item.ModBlockItem;

public interface IKiwiBlock extends BlockPickInteractionAware {

	default MutableComponent getName(ItemStack stack) {
		return Component.translatable(stack.getDescriptionId());
	}

	default BlockItem createItem(Item.Properties builder) {
		return new ModBlockItem((Block) this, builder);
	}

	default ItemStack getCloneItemStack(
			LevelReader level,
			BlockPos blockPos,
			BlockState blockState,
			@Nullable Player player,
			@Nullable HitResult hit) {
		return ModBlock.pickBlockEntityData(level, blockPos, blockState, blockState.getBlock().getCloneItemStack(level, blockPos, blockState));
	}

	@Override
	default ItemStack getPickedStack(BlockState state, BlockGetter view, BlockPos pos, Player player, HitResult result) {
		return getCloneItemStack(player.level(), pos, state, player, result);
	}
}
