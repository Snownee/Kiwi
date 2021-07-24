package snownee.kiwi.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * @since 2.7.0
 */
public class InvHandlerWrapper implements Container {

	protected final IItemHandlerModifiable handler;

	public InvHandlerWrapper(IItemHandlerModifiable handler) {
		this.handler = handler;
	}

	@Override
	public void clearContent() {
		int size = getContainerSize();
		for (int i = 0; i < size; i++) {
			removeItemNoUpdate(i);
		}
	}

	@Override
	public int getContainerSize() {
		return handler.getSlots();
	}

	@Override
	public boolean isEmpty() {
		int size = getContainerSize();
		for (int i = 0; i < size; i++) {
			if (!getItem(i).isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack getItem(int index) {
		return handler.getStackInSlot(index);
	}

	@Override
	public ItemStack removeItem(int index, int count) {
		ItemStack stack = getItem(index);
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack ret = stack.split(count);
		setItem(index, stack);
		return ret;
	}

	@Override
	public ItemStack removeItemNoUpdate(int index) {
		ItemStack stack = getItem(index);
		setItem(index, ItemStack.EMPTY);
		return stack;
	}

	@Override
	public void setItem(int index, ItemStack stack) {
		handler.setStackInSlot(index, stack);
	}

	@Override
	public void setChanged() {
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public boolean canPlaceItem(int index, ItemStack stack) {
		return handler.isItemValid(index, stack);
	}

}
