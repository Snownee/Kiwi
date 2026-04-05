package snownee.kiwi.util.client;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.mixin.forge.BlockColorsAccess;

public class ColorProviderUtil {
	public static BlockTintSource delegateBlock(Block block) {
		return new BlockTintSourceDelegate(block);
	}

	private static class BlockTintSourceDelegate implements BlockTintSource {
		private final Block provider;

		BlockTintSourceDelegate(Block provider) {
			this.provider = provider;
		}

		private List<BlockTintSource> sources() {
			BlockColorsAccess blockColors = (BlockColorsAccess) Minecraft.getInstance().getBlockColors();
			List<BlockTintSource> list = blockColors.getBlockColors().get(provider);
			return list != null ? list : List.of();
		}

		@Override
		public int color(BlockState state) {
			List<BlockTintSource> srcs = sources();
			return srcs.isEmpty() ? -1 : srcs.get(0).color(state);
		}

		@Override
		public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
			List<BlockTintSource> srcs = sources();
			return srcs.isEmpty() ? -1 : srcs.get(0).colorInWorld(state, level, pos);
		}
	}
}