package snownee.kiwi.block;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import snownee.kiwi.item.ModBlockItem;

public interface IKiwiBlock extends IBlockExtension {

	default MutableComponent getName(ItemStack stack) {
		return stack.getDisplayName().copy();
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
		return ModBlock.pickBlockEntityData(
				level,
				blockPos,
				blockState,
				blockState.getBlock().getCloneItemStack(level, blockPos, blockState, false, player));
	}

	@Override
	default ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean usePickBlockGameMasterBlocks, Player player) {
		return getCloneItemStack(level, pos, state, player, null);
	}
}
