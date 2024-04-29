package snownee.kiwi.customization.placement;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import snownee.kiwi.Kiwi;
import snownee.kiwi.customization.CustomizationHooks;
import snownee.kiwi.customization.block.KBlockSettings;
import snownee.kiwi.customization.duck.KPlayer;
import snownee.kiwi.customization.network.SSyncPlaceCountPacket;
import snownee.kiwi.util.Util;

public class PlacementSystem {
	private static final Cache<BlockPlaceContext, PlaceMatchResult> RESULT_CONTEXT = CacheBuilder.newBuilder().weakKeys().expireAfterWrite(
			100,
			TimeUnit.MILLISECONDS).build();

	public static boolean isDebugEnabled(Player player) {
		return player != null && player.isCreative() && player.getOffhandItem().is(Items.CHAINMAIL_HELMET);
	}

	public static void removeDebugBlocks(Level level, BlockPos start) {
		BlockPos.MutableBlockPos pos = start.mutable();
		pos.move(Direction.UP, 2);
		while (isBlockToRemove(level.getBlockState(pos))) {
			level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
			pos.move(Direction.UP);
		}
	}

	private static boolean isBlockToRemove(BlockState blockState) {
		if (blockState.is(Blocks.BEDROCK)) {
			return true;
		}
		String namespace = BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).getNamespace();
		return CustomizationHooks.getNamespaces().contains(namespace);
	}

	public static BlockState onPlace(BlockItem blockItem, BlockState blockState, BlockPlaceContext context) {
		PlaceChoices choices = null;
		{
			KBlockSettings settings = KBlockSettings.of(blockState.getBlock());
			if (settings != null) {
				choices = settings.placeChoices;
			}
		}
		if (choices != null && !choices.alter().isEmpty()) {
			for (PlaceChoices.Alter alter : choices.alter()) {
				BlockState altered = alter.alter(blockItem, context);
				if (altered != null) {
					return onPlace(blockItem, altered, context);
				}
			}
		}
		if (PlaceSlot.hasNoSlots(blockState.getBlock())) {
			return blockState;
		}
		if (context.isSecondaryUseActive() && (choices == null || choices.skippable())) {
			return blockState;
		}
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockPos.MutableBlockPos mutable = pos.mutable();
		var neighborSlots = Direction.stream()
				.map($ -> PlaceSlot.find(level.getBlockState(mutable.setWithOffset(pos, $)), $.getOpposite()))
				.filter(Predicate.not(Collection::isEmpty))
				.collect(Collectors.toUnmodifiableMap(
						$ -> $.iterator().next().side().getOpposite(),
						Function.identity()
				));
		if (neighborSlots.isEmpty()) {
			return blockState;
		}
		boolean debug = isDebugEnabled(context.getPlayer());
		List<PlaceMatchResult> results = Lists.newArrayList();
		boolean waterLoggable = blockState.hasProperty(BlockStateProperties.WATERLOGGED);
		boolean hasWater = waterLoggable && blockState.getValue(BlockStateProperties.WATERLOGGED);
		BlockState noWaterBlockState = hasWater ? blockState.setValue(BlockStateProperties.WATERLOGGED, false) : blockState;
		PlaceMatchResult originalResult = null;
		for (BlockState possibleState : blockState.getBlock().getStateDefinition().getPossibleStates()) {
			if (waterLoggable && hasWater != possibleState.getValue(BlockStateProperties.WATERLOGGED)) {
				continue;
			}
			int bonusInterest = 0;
			if (choices != null) {
				bonusInterest = choices.test(blockState, possibleState);
				if (bonusInterest == Integer.MIN_VALUE) {
					continue;
				}
			}
			PlaceMatchResult result = getPlaceMatchResultAt(possibleState, neighborSlots, bonusInterest);
			if (result != null) {
				results.add(result);
				if (possibleState == noWaterBlockState) {
					originalResult = result;
				}
			}
		}
		if (results.isEmpty()) {
			if (debug && !level.isClientSide) {
				Kiwi.LOGGER.info("No match");
				level.setBlockAndUpdate(mutable.move(Direction.UP), Blocks.BEDROCK.defaultBlockState());
			}
			return blockState;
		}
		results.sort(null);
		int resultIndex = 0;
		int maxInterest = results.get(0).interest();
		if (maxInterest > 0 && results.size() > 1 && context.getPlayer() instanceof KPlayer player) {
			for (int i = 1; i < results.size(); i++) {
				if (results.get(i).interest() < maxInterest) {
					break;
				}
				resultIndex = i;
			}
			if (resultIndex > 0) {
				resultIndex = player.kiwi$getPlaceCount() % (resultIndex + 1);
			}
		}
		PlaceMatchResult result = maxInterest == 0 ? originalResult : results.get(resultIndex);
		if (result == null) {
			return blockState;
		}
		if (debug && maxInterest > 0 && !level.isClientSide) {
			mutable.setWithOffset(pos, Direction.UP);
			Kiwi.LOGGER.info("Interest: %d".formatted(result.interest()));
			results.forEach($ -> {
				if ($ == result) {
					return;
				}
				level.setBlockAndUpdate(mutable.move(Direction.UP), $.blockState());
				Kiwi.LOGGER.info("Alt Interest: %d : %s".formatted($.interest(), $.blockState()));
			});
		}
		BlockState resultState = result.blockState();
		for (SlotLink.MatchResult link : result.links()) {
			resultState = link.onLinkFrom().apply(level, pos, resultState);
		}
		RESULT_CONTEXT.put(context, result);
		return resultState;
	}

	//TODO cache based on BlockStates and Direction, see Block.OCCLUSION_CACHE
	@Nullable
	public static PlaceMatchResult getPlaceMatchResultAt(
			BlockState blockState,
			Map<Direction, Collection<PlaceSlot>> theirSlotsMap,
			int bonusInterest) {
		int interest = 0;
		List<SlotLink.MatchResult> results = List.of();
		List<Vec3i> offsets = List.of();
		for (Direction side : Util.DIRECTIONS) {
			Collection<PlaceSlot> theirSlots = theirSlotsMap.get(side);
			if (theirSlots == null) {
				continue;
			}
			Collection<PlaceSlot> ourSlots = PlaceSlot.find(blockState, side);
			SlotLink.MatchResult result = SlotLink.find(ourSlots, theirSlots);
			if (result != null) {
				SlotLink link = result.link();
				interest += link.interest();
				if (results.isEmpty()) {
					results = Lists.newArrayListWithExpectedSize(theirSlotsMap.size());
					offsets = Lists.newArrayListWithExpectedSize(theirSlotsMap.size());
				}
				results.add(result);
				offsets.add(side.getNormal());
			}
		}
		if (interest < 0) {
			return null;
		}
		return new PlaceMatchResult(blockState, interest + bonusInterest, results, offsets);
	}

	public static void onBlockPlaced(BlockPlaceContext context) {
		PlaceMatchResult result = RESULT_CONTEXT.getIfPresent(context);
		if (result == null) {
			return;
		}
		RESULT_CONTEXT.invalidate(context);
		BlockPos.MutableBlockPos mutable = context.getClickedPos().mutable();
		for (int i = 0; i < result.links().size(); i++) {
			BlockPos theirPos = mutable.setWithOffset(context.getClickedPos(), result.offsets().get(i));
			BlockState theirState = context.getLevel().getBlockState(theirPos);
			SlotLink.MatchResult link = result.links().get(i);
			theirState = link.onLinkTo().apply(context.getLevel(), theirPos, theirState);
			context.getLevel().setBlock(theirPos, theirState, 11);
		}
		Player player = context.getPlayer();
		if (player != null) {
			((KPlayer) player).kiwi$incrementPlaceCount();
			if (player instanceof ServerPlayer serverPlayer) {
				SSyncPlaceCountPacket.sync(serverPlayer);
			}
		}
	}

	public static void onBlockRemoved(Level level, BlockPos pos, BlockState oldState, BlockState newState) {
		if (PlaceSlot.hasNoSlots(oldState.getBlock())) {
			return;
		}
		BlockPos.MutableBlockPos mutable = pos.mutable();
		for (Direction direction : Util.DIRECTIONS) {
			BlockState neighborState = level.getBlockState(mutable.setWithOffset(pos, direction));
			if (PlaceSlot.hasNoSlots(neighborState.getBlock())) {
				continue;
			}
			SlotLink.MatchResult result = SlotLink.find(oldState, neighborState, direction);
			if (result != null) {
				neighborState = result.onUnlinkTo().apply(level, mutable, neighborState);
				level.setBlockAndUpdate(mutable, neighborState);
			}
		}
	}
}
