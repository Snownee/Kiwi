package snownee.kiwi.customization.block;

import net.minecraft.world.level.block.Block;

public class BasicBlock extends Block implements CheckedWaterloggedBlock, KBlockUtils {
	public BasicBlock(Properties properties) {
		super(properties);
	}
}
