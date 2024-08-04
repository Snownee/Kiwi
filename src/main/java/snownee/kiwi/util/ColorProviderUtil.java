package snownee.kiwi.util;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ColorProviderUtil {
	public static BlockColor delegate(Block block) {
		return new BlockDelegate(() -> ColorProviderRegistry.BLOCK.get(block));
	}

	public static ItemColor delegate(Item item) {
		return new ItemDelegate(() -> ColorProviderRegistry.ITEM.get(item));
	}

	public static ItemColor delegateItemFallback(Block block) {
		return new ItemDelegate(() -> {
			BlockColor blockColor = ColorProviderRegistry.BLOCK.get(block);
			if (blockColor == null) {
				return null;
			} else {
				return (stack, i) -> blockColor.getColor(block.defaultBlockState(), null, null, i);
			}
		});
	}

	public static class Dummy implements ItemColor, BlockColor {
		public static final Dummy INSTANCE = new Dummy();

		@Override
		public int getColor(BlockState blockState, @Nullable BlockAndTintGetter blockAndTintGetter, @Nullable BlockPos blockPos, int i) {
			return -1;
		}

		@Override
		public int getColor(ItemStack itemStack, int i) {
			return -1;
		}
	}

	private static class BlockDelegate extends CachedSupplier<BlockColor> implements BlockColor {
		public BlockDelegate(Supplier<BlockColor> getter) {
			super(getter, Dummy.INSTANCE);
		}

		@Override
		public int getColor(BlockState blockState, @Nullable BlockAndTintGetter blockAndTintGetter, @Nullable BlockPos blockPos, int i) {
			return this.get().getColor(blockState, blockAndTintGetter, blockPos, i);
		}
	}

	private static class ItemDelegate extends CachedSupplier<ItemColor> implements ItemColor {
		public ItemDelegate(Supplier<ItemColor> getter) {
			super(getter, Dummy.INSTANCE);
		}

		@Override
		public int getColor(ItemStack itemStack, int i) {
			return this.get().getColor(itemStack, i);
		}
	}
}