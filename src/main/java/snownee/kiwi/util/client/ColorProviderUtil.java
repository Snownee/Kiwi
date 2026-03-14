package snownee.kiwi.util.client;

public class ColorProviderUtil {
//	public static BlockColor delegate(Block block) {
//		return new BlockDelegate(() -> Minecraft.getInstance().getBlockColors().blockColors.byId(BuiltInRegistries.BLOCK.getId(block)));
//	}
//
//	public static class Dummy implements BlockColor {
//		public static final Dummy INSTANCE = new Dummy();
//
//		@Override
//		public int getColor(BlockState blockState, @Nullable BlockAndTintGetter blockAndTintGetter, @Nullable BlockPos blockPos, int i) {
//			return -1;
//		}
//	}
//
//	private static class BlockDelegate extends CachedSupplier<BlockColor> implements BlockColor {
//		public BlockDelegate(Supplier<@Nullable BlockColor> getter) {
//			super(getter, Dummy.INSTANCE);
//		}
//
//		@Override
//		public int getColor(BlockState blockState, @Nullable BlockAndTintGetter blockAndTintGetter, @Nullable BlockPos blockPos, int i) {
//			return Objects.requireNonNull(this.get()).getColor(blockState, blockAndTintGetter, blockPos, i);
//		}
//	}
}