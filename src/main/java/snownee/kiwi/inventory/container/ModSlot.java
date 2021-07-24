package snownee.kiwi.inventory.container;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * @since 2.7.0
 */
public class ModSlot extends Slot {

	public ModSlot(Container inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return container.canPlaceItem(index, stack);
	}

}
