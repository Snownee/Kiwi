package snownee.kiwi.block;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import snownee.kiwi.block.entity.ModBlockEntity;

/**
 *
 * @author Snownee
 *
 */
public class ModBlock extends Block implements IKiwiBlock {

	public ModBlock(Block.Properties builder) {
		super(builder);
	}

	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
		return pick(state, target, world, pos, player);
	}

	public static ItemStack pick(BlockState state, @Nullable HitResult target, BlockGetter world, BlockPos pos, @Nullable Player player) {
		ItemStack stack = state.getBlock().getCloneItemStack(world, pos, state);
		BlockEntity tile = world.getBlockEntity(pos);
		if (tile instanceof ModBlockEntity && !tile.onlyOpCanSetNbt() && ((ModBlockEntity) tile).persistData) {
			CompoundTag data = tile.saveWithFullMetadata();
			data.remove("x");
			data.remove("y");
			data.remove("z");
			BlockItem.setBlockEntityData(stack, tile.getType(), data);
		}
		return stack;
	}

}
