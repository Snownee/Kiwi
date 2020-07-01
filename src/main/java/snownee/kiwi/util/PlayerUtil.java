package snownee.kiwi.util;

import javax.annotation.Nullable;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DirectionalPlaceContext;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;

public final class PlayerUtil {
    private PlayerUtil() {}

    @Nullable
    public static BlockPos tryPlace(World world, BlockPos pos, Direction side, @Nullable PlayerEntity player, Hand hand, BlockState state, @Nullable ItemStack stack, boolean playSound, boolean skipCollisionCheck) {
        BlockState worldState = world.getBlockState(pos);
        if (worldState.getBlock() == Blocks.SNOW && worldState./*has*/func_235901_b_(SnowBlock.LAYERS) && worldState.get(SnowBlock.LAYERS) < 8) {} else if (!state.isReplaceable(new DirectionalPlaceContext(world, pos, side.getOpposite(), stack == null ? ItemStack.EMPTY : stack, side.getOpposite()))) {
            pos = pos.offset(side);
        }
        if (skipCollisionCheck) {
            return tryPlace(world, pos, side.getOpposite(), player, hand, state, stack, playSound) ? pos : null;
        }
        ISelectionContext iselectioncontext = player == null ? ISelectionContext.dummy() : ISelectionContext.forEntity(player);
        if (world.func_226663_a_(state, pos, iselectioncontext)) {
            return tryPlace(world, pos, side.getOpposite(), player, hand, state, stack, playSound) ? pos : null;
        }
        return null;
    }

    public static boolean tryPlace(World world, BlockPos pos, Direction direction, @Nullable PlayerEntity player, Hand hand, BlockState state, @Nullable ItemStack stack, boolean playSound) {
        if (!world.isBlockModifiable(player, pos)) {
            return false;
        }
        if (player != null && !player.canPlayerEdit(pos, direction, stack)) {
            return false;
        }
        BlockSnapshot blocksnapshot = BlockSnapshot.create(world, pos);
        if (!world.setBlockState(pos, state)) {
            return false;
        }
        if (ForgeEventFactory.onBlockPlace(player, blocksnapshot, direction)) {
            blocksnapshot.restore(true, false);
            return false;
        }
        world.setBlockState(pos, state, 11);

        BlockState actualState = world.getBlockState(pos);

        if (stack != null) {
            BlockItem.setTileEntityNBT(world, player, pos, stack);

            if (player != null) {
                player.addStat(Stats.ITEM_USED.get(stack.getItem()));
                if (player instanceof ServerPlayerEntity) {
                    CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayerEntity) player, pos, stack);
                }
                actualState.getBlock().onBlockPlacedBy(world, pos, state, player, stack);
            }

            if (player == null || !player.abilities.isCreativeMode) {
                stack.shrink(1);
            }
        }

        if (playSound) {
            SoundType soundtype = actualState.getBlock().getSoundType(actualState, world, pos, player);
            world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
        }

        return true;
    }

    public static boolean canTouch(PlayerEntity player, BlockPos pos) {
        double reach = player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue();
        return player.getDistanceSq(pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d) <= reach * reach;
    }
}
