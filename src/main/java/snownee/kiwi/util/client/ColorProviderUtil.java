package snownee.kiwi.util.client;

import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import snownee.kiwi.mixin.forge.BlockColorsAccess;
import snownee.kiwi.util.CachedSupplier;

public class ColorProviderUtil {
	public static BlockTintSource delegate(Block block, int layer) {
		return new BlockDelegate(() -> {
			BlockColorsAccess blockColors = (BlockColorsAccess) Minecraft.getInstance().getBlockColors();
			var list = blockColors.getBlockColors().get(block);
			return list != null && layer >= 0 && layer < list.size() ? list.get(layer) : Dummy.INSTANCE;
		});
	}

	public static class Dummy implements BlockTintSource {
		public static final Dummy INSTANCE = new Dummy();

		@Override
		public int color(BlockState state) {
			return -1;
		}
	}

	private static class BlockDelegate extends CachedSupplier<BlockTintSource> implements BlockTintSource {
		public BlockDelegate(Supplier<@Nullable BlockTintSource> getter) {
			super(getter, Dummy.INSTANCE);
		}

		@Override
		public int color(BlockState state) {
			return Objects.requireNonNull(get()).color(state);
		}

		@Override
		public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
			return Objects.requireNonNull(get()).colorInWorld(state, level, pos);
		}

		@Override
		public int colorAsTerrainParticle(BlockState state, BlockAndTintGetter level, BlockPos pos) {
			return Objects.requireNonNull(get()).colorAsTerrainParticle(state, level, pos);
		}

		@Override
		public Set<Property<?>> relevantProperties() {
			return Objects.requireNonNull(get()).relevantProperties();
		}
	}
}