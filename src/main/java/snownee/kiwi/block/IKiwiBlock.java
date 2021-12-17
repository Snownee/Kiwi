package snownee.kiwi.block;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.block.BlockPickInteractionAware;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import snownee.kiwi.item.ModBlockItem;

public interface IKiwiBlock extends BlockPickInteractionAware {

	default MutableComponent getName(ItemStack stack) {
		return new TranslatableComponent(stack.getDescriptionId());
	}

	default BlockItem createItem(Item.Properties builder) {
		return new ModBlockItem((Block) this, builder);
	}

	@Override
	default ItemStack getPickedStack(BlockState state, BlockGetter view, BlockPos pos, @Nullable Player player, @Nullable HitResult result) {
		return ModBlock.pick(state, result, view, pos, player);
	}

}
