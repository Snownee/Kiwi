package snownee.kiwi.block;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import snownee.kiwi.block.entity.ModBlockEntity;

/**
 * @author Snownee
 */
public class ModBlock extends Block implements IKiwiBlock {

	public ModBlock(Block.Properties builder) {
		super(builder);
	}

	public static ItemStack pick(LevelReader level, BlockPos pos, BlockState state, @Nullable Player player, @Nullable HitResult hit) {
		ItemStack stack = state.getBlock().getCloneItemStack(level, pos, state);
		BlockEntity tile = level.getBlockEntity(pos);
		if (tile instanceof ModBlockEntity && !tile.onlyOpCanSetNbt() && ((ModBlockEntity) tile).persistData) {
			CompoundTag data = tile.saveWithFullMetadata(level.registryAccess());
			data.remove("x");
			data.remove("y");
			data.remove("z");
			BlockItem.setBlockEntityData(stack, tile.getType(), data);
		}
		return stack;
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader level, BlockPos blockPos, BlockState blockState) {
		return getCloneItemStack(level, blockPos, blockState, null, null);
	}
}
