package snownee.kiwi.customization.builder;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.Kiwi;
import snownee.kiwi.customization.block.family.BlockFamily;
import snownee.kiwi.util.codec.KCodecs;

public record ReplaceInHandRule(Map<BlockFamily, Object> families, BlockSpread spread) implements BuilderRule {
	public static final MapCodec<ReplaceInHandRule> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
					KCodecs.compactList(BlockFamily.CODEC).fieldOf("family").forGetter($ -> List.copyOf($.families().keySet())),
					BlockSpread.CODEC.fieldOf("spread").forGetter(ReplaceInHandRule::spread))
			.apply(instance, ReplaceInHandRule::new));

	public ReplaceInHandRule(List<BlockFamily> families, BlockSpread spread) {
		this(families.stream().collect(Collectors.toMap(Function.identity(), Function.identity())), spread);
	}

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
		if (!(itemStack.getItem() instanceof BlockItem item)) {
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
				level.setBlock(pos, oldBlock, Block.UPDATE_CLIENTS);
			} else {
				success = true;
			}
			if (player != null) {
				player.setItemInHand(context.getHand(), itemStack);
			}
		}
		if (success && player != null) {
			playPlaceSound(player, item.getBlock().defaultBlockState());
		}
	}

	@Override
	public List<BlockPos> searchPositions(BlockState blockState, UseOnContext context) {
		List<BlockPos> list = List.of();
		try {
			list = spread.collect(context, $ -> $.is(blockState.getBlock()), null);
		} catch (Exception e) {
			Kiwi.LOGGER.error("Failed to collect positions", e);
		}
		return list;
	}

	@Override
	public Type<?> type() {
		return BuilderRuleTypes.REPLACE_IN_HAND.getOrCreate();
	}
}
