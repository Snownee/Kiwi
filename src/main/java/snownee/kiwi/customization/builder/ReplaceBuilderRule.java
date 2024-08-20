package snownee.kiwi.customization.builder;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.google.common.base.Predicates;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.Kiwi;
import snownee.kiwi.customization.block.family.BlockFamily;

public record ReplaceBuilderRule(Map<BlockFamily, Object> families, BlockSpread spread) implements BuilderRule {
	@Override
	public Stream<Block> relatedBlocks() {
		return families.keySet().stream().flatMap(BlockFamily::blocks);
	}

	@Override
	public boolean matches(Player player, ItemStack itemStack, BlockState blockState) {
		if (itemStack.is(blockState.getBlock().asItem())) {
			return false;
		}
		return relatedBlocks().anyMatch(block -> itemStack.is(block.asItem()));
	}

	@Override
	public void apply(UseOnContext context, List<BlockPos> positions) {
		ItemStack itemStack = context.getItemInHand().copy();
		if (!(itemStack.getItem().asItem() instanceof BlockItem item)) {
			return;
		}
		BlockPlaceContext placeContext = new BlockPlaceContext(context);
		Player player = context.getPlayer();
		Level level = context.getLevel();
		boolean success = false;
		for (BlockPos pos : positions) {
			BlockState oldBlock = level.getBlockState(pos);
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_INVISIBLE); //FIXME water
			placeContext = BlockPlaceContext.at(placeContext, pos, context.getClickedFace());
			if (item.place(placeContext) == InteractionResult.FAIL) {
				level.setBlock(pos, oldBlock, Block.UPDATE_INVISIBLE);
			} else {
				success = true;
			}
			if (player != null) {
				player.setItemInHand(context.getHand(), itemStack);
			}
		}
		if (success && player != null) {
			BlockState blockState = item.getBlock().defaultBlockState();
			SoundType soundType = blockState.getSoundType();
			level.playSound(
					null,
					player.blockPosition(),
					soundType.getPlaceSound(),
					SoundSource.BLOCKS,
					(soundType.getVolume() + 1.0F) / 2.0F,
					soundType.getPitch() * 0.8F);
		}
	}

	@Override
	public List<BlockPos> searchPositions(UseOnContext context) {
		List<BlockPos> list = List.of();
		try {
			list = spread.collect(context, Predicates.alwaysTrue());
		} catch (Exception e) {
			Kiwi.LOGGER.error("Failed to collect positions", e);
		}
		return list;
	}
}
