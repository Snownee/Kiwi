package snownee.kiwi.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.extensions.IForgeBlock;
import snownee.kiwi.item.ModBlockItem;

public interface IKiwiBlock extends IForgeBlock {

	default MutableComponent getName(ItemStack stack) {
		return Component.translatable(stack.getDescriptionId());
	}

	default BlockItem createItem(Item.Properties builder) {
		return new ModBlockItem((Block) this, builder);
	}

	@Override
	default ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
		return ModBlock.pick(state, target, world, pos, player);
	}

}
