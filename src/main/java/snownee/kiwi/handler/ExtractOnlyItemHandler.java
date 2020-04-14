package snownee.kiwi.handler;

import java.util.function.Supplier;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ExtractOnlyItemHandler<T extends IItemHandler> implements IItemHandler, Supplier<T> {

    private final T handler;

    public ExtractOnlyItemHandler(T handler) {
        this.handler = handler;
    }

    @Override
    public int getSlots() {
        return handler.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return handler.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return handler.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return handler.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return false;
    }

    @Override
    public T get() {
        return handler;
    }

}
