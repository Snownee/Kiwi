//package snownee.kiwi.util;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import net.minecraft.inventory.IInventory;
//import net.minecraft.item.ItemStack;
//import net.minecraftforge.items.IItemHandler;
//import snownee.kiwi.crafting.input.ProcessingInput;
//
//public class InventoryUtil
//{
//    private InventoryUtil()
//    {
//    }
//
//    public static boolean canMergeStacks(IInventory inventory, ItemStack stack1, ItemStack stack2)
//    {
//        return !stack1.isEmpty() && stackEqualExact(stack1, stack2) && stack1.isStackable() && stack1.getCount() < stack1.getMaxStackSize() && stack1.getCount() < inventory.getInventoryStackLimit();
//    }
//
//    public static boolean stackEqualExact(ItemStack stack1, ItemStack stack2)
//    {
//        return stack1.getItem() == stack2.getItem() && (!stack1.getHasSubtypes() || stack1.getMetadata() == stack2.getMetadata()) && ItemStack.areItemStackTagsEqual(stack1, stack2);
//    }
//
//    // TODO: check size limit
//    public static List<ItemStack> mergeItemStacks(List<ItemStack> stacks, boolean checkLimit)
//    {
//        List<ItemStack> newStacks = new ArrayList<>(stacks.size());
//        for (ItemStack stack : stacks)
//        {
//            if (stack.isEmpty())
//            {
//                continue;
//            }
//            boolean flag = true;
//            for (ItemStack newStack : newStacks)
//            {
//                if (newStack.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(stack, newStack))
//                {
//                    newStack.grow(stack.getCount());
//                    flag = false;
//                    break;
//                }
//            }
//            if (flag)
//            {
//                newStacks.add(stack.copy());
//            }
//        }
//        return newStacks;
//    }
//
//    public static boolean consumeItemStack(IItemHandler itemHandler, ItemStack stack, boolean simulated)
//    {
//        if (stack.isEmpty())
//        {
//            return true;
//        }
//        stack = stack.copy();
//        for (int i = 0; i < itemHandler.getSlots(); ++i)
//        {
//            if (stack.isItemEqual(itemHandler.getStackInSlot(i)))
//            {
//                ItemStack extracted = itemHandler.extractItem(i, stack.getCount(), simulated);
//                if (!extracted.isEmpty())
//                {
//                    stack.shrink(extracted.getCount());
//                    if (stack.isEmpty())
//                    {
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }
//
//    public static boolean consumeItemStack(IItemHandler itemHandler, ProcessingInput input, int count, boolean simulated)
//    {
//        if (input.isEmpty())
//        {
//            return true;
//        }
//        for (int i = 0; i < itemHandler.getSlots(); ++i)
//        {
//            if (input.matches(itemHandler.getStackInSlot(i)))
//            {
//                ItemStack extracted = itemHandler.extractItem(i, count, simulated);
//                if (!extracted.isEmpty())
//                {
//                    count -= extracted.getCount();
//                    if (count == 0)
//                    {
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }
//
//}
