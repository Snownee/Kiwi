package snownee.kiwi.block.entity;

//package snownee.kiwi.tile;
//
//import java.util.List;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//
//import net.minecraft.init.Items;
//import net.minecraft.item.ItemStack;
//import net.minecraft.nbt.NBTTagCompound;
//import net.minecraft.util.EnumFacing;
//import net.minecraftforge.common.capabilities.Capability;
//import net.minecraftforge.common.util.Constants;
//import net.minecraftforge.items.CapabilityItemHandler;
//import net.minecraftforge.items.ItemStackHandler;
//
//public class TileInventoryBase extends TileBase
//{
//    public class StackHandler extends ItemStackHandler
//    {
//        private final int stackLimit;
//
//        @Deprecated
//        StackHandler(TileInventoryBase tile, int slot, int stackLimit)
//        {
//            this(slot, stackLimit);
//        }
//
//        StackHandler(int slot, int stackLimit)
//        {
//            super(slot);
//            this.stackLimit = stackLimit;
//        }
//
//        @Override
//        public int getSlotLimit(int slot)
//        {
//            return stackLimit;
//        }
//
//        public List<ItemStack> getStacks()
//        {
//            return stacks;
//        }
//
//        @Override
//        public boolean isItemValid(int slot, ItemStack stack)
//        {
//            return isItemValidForSlot(slot, stack);
//        }
//
//        @Nonnull
//        @Override
//        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
//        {
//            if (!isItemValid(slot, stack))
//            {
//                return stack;
//            }
//            return super.insertItem(slot, stack, simulate);
//        }
//
//        @Override
//        protected void onContentsChanged(int slot)
//        {
//            TileInventoryBase.this.onContentsChanged(slot);
//        }
//    }
//
//    public final StackHandler stacks;
//
//    @SuppressWarnings("deprecation")
//    public TileInventoryBase(int slot)
//    {
//        this(slot, Items.AIR.getItemStackLimit()); // Default to a standard stack
//    }
//
//    public TileInventoryBase(int slot, int stackLimit)
//    {
//        stacks = new StackHandler(slot, stackLimit);
//    }
//
//    @Override
//    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
//    {
//        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
//    }
//
//    @Override
//    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
//    {
//        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
//        {
//            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(stacks);
//        }
//        return super.getCapability(capability, facing);
//    }
//
//    @Override
//    public void readFromNBT(NBTTagCompound compound)
//    {
//        super.readFromNBT(compound);
//        if (compound.hasKey("Items", Constants.NBT.TAG_COMPOUND))
//        {
//            stacks.deserializeNBT(compound.getCompoundTag("Items"));
//        }
//    }
//
//    @Nonnull
//    @Override
//    public NBTTagCompound writeToNBT(NBTTagCompound compound)
//    {
//        super.writeToNBT(compound);
//        compound.setTag("Items", stacks.serializeNBT());
//        return compound;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    protected void readPacketData(NBTTagCompound data)
//    {
//        this.stacks.deserializeNBT(data.getCompoundTag("Items"));
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Nonnull
//    @Override
//    protected NBTTagCompound writePacketData(NBTTagCompound data)
//    {
//        data.setTag("Items", this.stacks.serializeNBT());
//        return data;
//    }
//
//    public boolean isItemValidForSlot(int index, ItemStack stack)
//    {
//        return true;
//    }
//
//    public void onContentsChanged(int slot)
//    {
//        refresh();
//    }
//
//}
