package snownee.kiwi.util.client;

import java.util.Objects;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.mixin.forge.BlockColorsAccess;
import snownee.kiwi.util.CachedSupplier;

public class ColorProviderUtil {
	public static BlockColor delegate(Block block) {
		return new BlockDelegate(() -> {
			BlockColorsAccess blockColors = (BlockColorsAccess) Minecraft.getInstance().getBlockColors();
			return blockColors.getBlockColors().get(block);
		});
	}

	public static class Dummy implements BlockColor {
		public static final Dummy INSTANCE = new Dummy();

		@Override
		public int getColor(BlockState blockState, @Nullable BlockAndTintGetter blockAndTintGetter, @Nullable BlockPos blockPos, int i) {
			return -1;
		}
	}

	private static class BlockDelegate extends CachedSupplier<BlockColor> implements BlockColor {
		public BlockDelegate(Supplier<BlockColor> getter) {
			super(getter, Dummy.INSTANCE);
		}

		@Override
		public int getColor(BlockState blockState, @Nullable BlockAndTintGetter blockAndTintGetter, @Nullable BlockPos blockPos, int i) {
			return Objects.requireNonNull(this.get()).getColor(blockState, blockAndTintGetter, blockPos, i);
		}
	}
}