package snownee.kiwi.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import snownee.kiwi.Kiwi;
import snownee.kiwi.block.entity.ModBlockEntity;

/**
 * @author Snownee
 */
public class ModBlock extends Block implements IKiwiBlock {

	public ModBlock(Properties builder) {
		super(builder);
	}

	public static ItemStack pickBlockEntityData(LevelReader level, BlockPos pos, BlockState blockState, ItemStack itemStack) {
		if (blockState.hasBlockEntity() && level.getBlockEntity(pos) instanceof ModBlockEntity be && be.persistData) {
			try (ProblemReporter.ScopedCollector collector = new ProblemReporter.ScopedCollector(be.problemPath(), Kiwi.LOGGER)) {
				TagValueOutput tagValueOutput = TagValueOutput.createWithContext(collector, level.registryAccess());
				be.saveCustomOnly(tagValueOutput);
				be.removeComponentsFromTag(tagValueOutput);
				BlockItem.setBlockEntityData(itemStack, be.getType(), tagValueOutput);
				itemStack.applyComponents(be.collectComponents());
			}
		}
		return itemStack;
	}
}
