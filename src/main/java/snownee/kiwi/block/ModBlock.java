package snownee.kiwi.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.block.entity.ModBlockEntity;

/**
 * @author Snownee
 */
public class ModBlock extends Block implements IKiwiBlock {

	public ModBlock(Block.Properties builder) {
		super(builder);
	}

	public static ItemStack pickBlockEntityData(LevelReader level, BlockPos pos, BlockState blockState, ItemStack itemStack) {
		if (blockState.hasBlockEntity() && level.getBlockEntity(pos) instanceof ModBlockEntity be && be.persistData) {
			CompoundTag data = be.saveWithFullMetadata(level.registryAccess());
			data.remove("x");
			data.remove("y");
			data.remove("z");
			BlockItem.setBlockEntityData(itemStack, be.getType(), data);
		}
		return itemStack;
	}
}
