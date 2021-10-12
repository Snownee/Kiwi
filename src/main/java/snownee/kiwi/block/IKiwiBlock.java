package snownee.kiwi.block;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import snownee.kiwi.item.ModBlockItem;

public interface IKiwiBlock {

	default Component getName(ItemStack stack) {
		return new TranslatableComponent(stack.getDescriptionId());
	}

	default BlockItem createItem(Item.Properties builder) {
		return new ModBlockItem((Block) this, builder);
	}

}
