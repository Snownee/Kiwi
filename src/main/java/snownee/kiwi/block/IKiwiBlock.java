package snownee.kiwi.block;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import snownee.kiwi.item.ModBlockItem;

public interface IKiwiBlock {

	default ITextComponent getName(ItemStack stack) {
		return new TranslationTextComponent(stack.getTranslationKey());
	}

	default BlockItem createItem(Item.Properties builder) {
		return new ModBlockItem((Block) this, builder);
	}

}
