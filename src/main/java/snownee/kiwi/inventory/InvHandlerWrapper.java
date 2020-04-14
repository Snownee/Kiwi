package snownee.kiwi.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * @since 2.7.0
 */
public class InvHandlerWrapper implements IInventory {

    protected final IItemHandlerModifiable handler;

    public InvHandlerWrapper(IItemHandlerModifiable handler) {
        this.handler = handler;
    }

    @Override
    public void clear() {
        int size = getSizeInventory();
        for (int i = 0; i < size; i++) {
            removeStackFromSlot(i);
        }
    }

    @Override
    public int getSizeInventory() {
        return handler.getSlots();
    }

    @Override
    public boolean isEmpty() {
        int size = getSizeInventory();
        for (int i = 0; i < size; i++) {
            if (!getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return handler.getStackInSlot(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack stack = getStackInSlot(index);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack ret = stack.split(count);
        setInventorySlotContents(index, stack);
        return ret;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = getStackInSlot(index);
        setInventorySlotContents(index, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        handler.setStackInSlot(index, stack);
    }

    @Override
    public void markDirty() {}

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        return true;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return handler.isItemValid(index, stack);
    }

}
