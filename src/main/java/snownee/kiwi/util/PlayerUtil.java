package snownee.kiwi.util;

import org.jetbrains.annotations.Nullable;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

public final class PlayerUtil {
	private PlayerUtil() {
	}

	@Nullable
	public static BlockPos tryPlace(
			Level world,
			BlockPos pos,
			Direction side,
			@Nullable Player player,
			InteractionHand hand,
			BlockState state,
			@Nullable ItemStack stack,
			boolean playSound,
			boolean skipCollisionCheck) {
		BlockState worldState = world.getBlockState(pos);
		if (worldState.getBlock() == Blocks.SNOW && worldState.hasProperty(SnowLayerBlock.LAYERS) &&
				worldState.getValue(SnowLayerBlock.LAYERS) < 8) {
		} else if (!state.canBeReplaced(new DirectionalPlaceContext(
				world,
				pos,
				side.getOpposite(),
				stack == null ? ItemStack.EMPTY : stack,
				side.getOpposite()))) {
			pos = pos.relative(side);
		}
		if (skipCollisionCheck) {
			return tryPlace(world, pos, side.getOpposite(), player, hand, state, stack, playSound) ? pos : null;
		}
		CollisionContext iselectioncontext = player == null ? CollisionContext.empty() : CollisionContext.of(player);
		if (world.isUnobstructed(state, pos, iselectioncontext)) {
			return tryPlace(world, pos, side.getOpposite(), player, hand, state, stack, playSound) ? pos : null;
		}
		return null;
	}

	public static boolean tryPlace(
			Level world,
			BlockPos pos,
			Direction direction,
			@Nullable Player player,
			InteractionHand hand,
			BlockState state,
			@Nullable ItemStack stack,
			boolean playSound) {
		if (!world.mayInteract(player, pos)) {
			return false;
		}
		if (player != null && !player.mayUseItemAt(pos, direction, stack)) {
			return false;
		}
		//		BlockSnapshot blocksnapshot = BlockSnapshot.create(world.dimension(), world, pos);
		//		if (!world.setBlockAndUpdate(pos, state)) {
		//			return false;
		//		}
		//		if (ForgeEventFactory.onBlockPlace(player, blocksnapshot, direction)) {
		//			blocksnapshot.restore(true, false);
		//			return false;
		//		}
		world.setBlock(pos, state, 11);

		BlockState actualState = world.getBlockState(pos);

		if (stack != null) {
			BlockItem.updateCustomBlockEntityTag(world, player, pos, stack);

			if (player != null) {
				player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
				if (player instanceof ServerPlayer) {
					CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) player, pos, stack);
				}
				actualState.getBlock().setPlacedBy(world, pos, state, player, stack);
			}

			if (player == null || !player.getAbilities().instabuild) {
				stack.shrink(1);
			}
		}

		if (playSound) {
			SoundType soundtype = actualState.getBlock().getSoundType(actualState);
			world.playSound(
					player,
					pos,
					soundtype.getPlaceSound(),
					SoundSource.BLOCKS,
					(soundtype.getVolume() + 1.0F) / 2.0F,
					soundtype.getPitch() * 0.8F);
		}

		return true;
	}

}
