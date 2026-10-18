package snownee.kiwi.customization.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.world.level.block.Block;

public class BasicBlock extends Block implements CheckedWaterloggedBlock, KBlockUtils {
	public static final MapCodec<Block> CODEC = snownee.kiwi.customization.block.loader.BlockCodecs.simpleCodec(BasicBlock::new);

	public BasicBlock(Properties properties) {
		super(properties);
	}

}
