package snownee.kiwi.util;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;

public final class PlayerUtil
{
    private PlayerUtil()
    {
    }

    public static BlockPos tryPlaceBlock(World world, BlockPos posPlacedOn, EnumFacing sidePlacedOn, @Nullable EntityPlayer player, EnumHand hand, IBlockState state, @Nullable ItemStack stack, boolean skipCollisionCheck)
    {
        Block block = world.getBlockState(posPlacedOn).getBlock();
        if (block == Blocks.SNOW_LAYER && block.isReplaceable(world, posPlacedOn))
        {
            sidePlacedOn = EnumFacing.UP;
        }
        else if (!block.isReplaceable(world, posPlacedOn))
        {
            posPlacedOn = posPlacedOn.offset(sidePlacedOn);
        }
        if (world.mayPlace(state.getBlock(), posPlacedOn, skipCollisionCheck, sidePlacedOn, player))
        {
            return tryPlaceBlock(world, posPlacedOn, sidePlacedOn, player, hand, state, stack) ? posPlacedOn : null;
        }
        return null;
    }

    public static boolean tryPlaceBlock(World world, BlockPos pos, EnumFacing sidePlacedOn, @Nullable EntityPlayer player, EnumHand hand, IBlockState state, @Nullable ItemStack stack)
    {
        if (!world.isBlockModifiable(player, pos))
        {
            return false;
        }
        if (player != null && stack != null && !player.canPlayerEdit(pos, sidePlacedOn, stack))
        {
            return false;
        }
        BlockSnapshot blocksnapshot = BlockSnapshot.getBlockSnapshot(world, pos);
        if (!world.setBlockState(pos, state))
        {
            return false;
        }
        if (player != null && ForgeEventFactory.onPlayerBlockPlace(player, blocksnapshot, sidePlacedOn, hand).isCanceled())
        {
            blocksnapshot.restore(true, false);
            return false;
        }
        world.setBlockState(pos, state, 11);

        IBlockState actualState = world.getBlockState(pos);

        if (stack != null)
        {
            ItemBlock.setTileEntityNBT(world, player, pos, stack);
            if (player != null)
            {
                player.addStat(StatList.getObjectUseStats(stack.getItem()));
                if (player instanceof EntityPlayerMP)
                {
                    CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, pos, stack);
                }
                actualState.getBlock().onBlockPlacedBy(world, pos, state, player, stack);
            }

            if (player == null || !player.capabilities.isCreativeMode)
            {
                stack.shrink(1);
            }
        }

        SoundType soundtype = actualState.getBlock().getSoundType(actualState, world, pos, player);
        world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

        return true;
    }

    public static ActionResult<ItemStack> mergeItemStack(ItemStack stack, EntityPlayer player, EnumHand hand)
    {
        InventoryPlayer inventory = player.inventory;
        List<Integer> slots = new ArrayList<>(inventory.mainInventory.size() + inventory.offHandInventory.size());

        int preferred;
        if (hand == EnumHand.MAIN_HAND)
            preferred = inventory.currentItem;
        else
            preferred = inventory.mainInventory.size() + inventory.armorInventory.size();

        slots.add(preferred);

        for (int i = 0; i < inventory.mainInventory.size(); i++)
        {
            if (i != preferred)
            {
                slots.add(i);
            }
        }
        for (int i = inventory.mainInventory.size() + inventory.armorInventory.size(); i < inventory.mainInventory.size() + inventory.armorInventory.size() + inventory.offHandInventory.size(); i++)
        {
            if (i != preferred)
            {
                slots.add(i);
            }
        }

        boolean result = false;

        if (stack.isStackable())
        {
            for (int i : slots)
            {
                if (stack.isEmpty())
                {
                    break;
                }
                ItemStack itemstack = inventory.getStackInSlot(i);

                if (!itemstack.isEmpty() && InventoryUtil.stackEqualExact(stack, itemstack))
                {
                    int j = itemstack.getCount() + stack.getCount();
                    int maxSize = Math.min(inventory.getInventoryStackLimit(), stack.getMaxStackSize());

                    if (j <= maxSize)
                    {
                        stack.setCount(0);
                        itemstack.setCount(j);
                        result = true;
                    }
                    else if (itemstack.getCount() < maxSize)
                    {
                        stack.shrink(maxSize - itemstack.getCount());
                        itemstack.setCount(maxSize);
                        result = true;
                    }
                }
            }
        }

        if (!stack.isEmpty())
        {
            for (int i : slots)
            {
                ItemStack itemstack1 = inventory.getStackInSlot(i);

                if (itemstack1.isEmpty() && inventory.isItemValidForSlot(i, stack))
                {
                    if (stack.getCount() > inventory.getInventoryStackLimit())
                    {
                        inventory.setInventorySlotContents(i, stack.splitStack(inventory.getInventoryStackLimit()));
                    }
                    else
                    {
                        inventory.setInventorySlotContents(i, stack.splitStack(stack.getCount()));
                    }
                    result = true;
                    break;
                }
            }
        }

        if (!stack.isEmpty())
        {
            player.dropItem(stack, false);
        }
        else
        {
            player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 1.0F, 1.0F);
        }
        return new ActionResult<>(result ? EnumActionResult.SUCCESS : EnumActionResult.FAIL, player.getHeldItem(hand));
    }

    public static boolean canTouch(EntityPlayer player, BlockPos pos)
    {
        double reach = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
        return player.getDistanceSqToCenter(pos) <= reach * reach;
    }
}
