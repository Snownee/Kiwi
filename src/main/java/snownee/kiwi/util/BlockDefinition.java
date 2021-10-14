package snownee.kiwi.util;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

public interface BlockDefinition {

	Map<String, Factory<?>> MAP = Maps.newConcurrentMap();
	List<Factory<?>> FACTORIES = Lists.newLinkedList();

	static void registerFactory(Factory<?> factory) {
		MAP.put(factory.getId(), factory);
		if (factory.getId().equals(SimpleBlockDefinition.TYPE)) {
			FACTORIES.add(factory);
		} else {
			FACTORIES.add(0, factory);
		}
	}

	static BlockDefinition fromNBT(CompoundTag tag) {
		Factory<?> factory = MAP.get(tag.getString("Type"));
		if (factory == null)
			return null;
		return factory.fromNBT(tag);
	}

	static BlockDefinition fromBlock(BlockState state, BlockEntity blockEntity, LevelReader level, BlockPos pos) {
		for (Factory<?> factory : FACTORIES) {
			BlockDefinition supplier = factory.fromBlock(state, blockEntity, level, pos);
			if (supplier != null) {
				return supplier;
			}
		}
		return null;
	}

	static BlockDefinition fromItem(ItemStack stack, BlockPlaceContext context) {
		if (!stack.isEmpty()) {
			for (Factory<?> factory : FACTORIES) {
				BlockDefinition supplier = factory.fromItem(stack, context);
				if (supplier != null) {
					return supplier;
				}
			}
		}
		return null;
	}

	Factory<?> getFactory();

	@OnlyIn(Dist.CLIENT)
	BakedModel model();

	@OnlyIn(Dist.CLIENT)
	default IModelData modelData() {
		return EmptyModelData.INSTANCE;
	}

	@OnlyIn(Dist.CLIENT)
	Material renderMaterial(@Nullable Direction direction);

	void save(CompoundTag tag);

	@OnlyIn(Dist.CLIENT)
	boolean canRenderInLayer(RenderType layer);

	boolean canOcclude();

	@OnlyIn(Dist.CLIENT)
	int getColor(BlockState blockState, BlockAndTintGetter level, BlockPos pos, int index);

	Component getDescription();

	void place(Level level, BlockPos pos);

	ItemStack createItem(HitResult target, BlockGetter world, @Nullable BlockPos pos, @Nullable Player player);

	BlockState getBlockState();

	SoundType getSoundType();

	interface Factory<T extends BlockDefinition> {
		T fromNBT(CompoundTag tag);

		String getId();

		@Nullable
		T fromBlock(BlockState state, BlockEntity blockEntity, LevelReader level, BlockPos pos);

		@Nullable
		T fromItem(ItemStack stack, BlockPlaceContext context);
	}

}
