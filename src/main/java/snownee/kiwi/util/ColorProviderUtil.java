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
	public static Supplier<BlockColor> delegate(Block block) {
		return new CachedSupplier<>(() -> ColorProviderRegistry.BLOCK.get(block), Dummy.INSTANCE);
	}

	public static Supplier<ItemColor> delegate(Item item) {
		return new CachedSupplier<>(() -> ColorProviderRegistry.ITEM.get(item), Dummy.INSTANCE);
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
}
