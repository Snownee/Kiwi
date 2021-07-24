package snownee.kiwi.util;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class InventoryUtil {
	private InventoryUtil() {
	}

	public static boolean consumeItemStack(IItemHandler itemHandler, ItemStack stack, boolean simulated) {
		if (stack.isEmpty()) {
			return true;
		}
		stack = stack.copy();
		for (int i = 0; i < itemHandler.getSlots(); ++i) {
			if (stack.sameItem(itemHandler.getStackInSlot(i))) {
				ItemStack extracted = itemHandler.extractItem(i, stack.getCount(), true);
				if (extracted.getCount() == stack.getCount()) {
					if (!simulated) {
						itemHandler.extractItem(i, stack.getCount(), false);
						stack.shrink(extracted.getCount());
					}
					return true;
				}
			}
		}
		return false;
	}

}
