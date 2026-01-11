package snownee.kiwi.customization.placement;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public record PlaceSlot(Direction side, ImmutableSortedMap<String, String> tags) {
	public static final Comparator<String> TAG_COMPARATOR = (a, b) -> {
		// make sure that the tag prefixed with * comes first
		boolean aStar = a.charAt(0) == '*';
		boolean bStar = b.charAt(0) == '*';
		if (aStar != bStar) {
			return aStar ? -1 : 1;
		}
		return a.compareTo(b);
	};
	private static ImmutableListMultimap<Pair<BlockState, Direction>, PlaceSlot> BLOCK_STATE_LOOKUP = ImmutableListMultimap.of();
	private static ImmutableSet<Block> BLOCK_HAS_SLOTS = ImmutableSet.of();

	public static Collection<PlaceSlot> find(BlockState blockState, Direction side) {
		if (blockState.hasProperty(BlockStateProperties.WATERLOGGED)) {
			blockState = blockState.setValue(BlockStateProperties.WATERLOGGED, false);
		}
		return BLOCK_STATE_LOOKUP.get(Pair.of(blockState, side));
	}

	public static Optional<PlaceSlot> find(BlockState blockState, Direction side, String primaryTag) {
		if (hasNoSlots(blockState.getBlock())) {
			return Optional.empty();
		}
		Collection<PlaceSlot> slots = find(blockState, side);
		if (slots.isEmpty()) {
			return Optional.empty();
		}
		return slots.stream().filter(slot -> slot.primaryTag().equals(primaryTag)).findAny();
	}

	public static boolean hasNoSlots(Block block) {
		return !BLOCK_HAS_SLOTS.contains(block);
	}

	public static void renewData(PlaceSlotProvider.Preparation preparation) {
		BLOCK_STATE_LOOKUP = ImmutableListMultimap.copyOf(preparation.slots());
		BLOCK_HAS_SLOTS = preparation.slots()
				.keySet()
				.stream()
				.map(Pair::getFirst)
				.map(BlockState::getBlock)
				.collect(ImmutableSet.toImmutableSet());
	}

	public static int blockCount() {
		return BLOCK_HAS_SLOTS.size();
	}

	public String primaryTag() {
		return tags.firstKey();
	}

	public List<String> tagList() {
		List<String> list = Lists.newArrayListWithCapacity(tags.size());
		tags.forEach((k, v) -> {
			if (v.isEmpty()) {
				list.add(k);
			} else {
				list.add(k + ":" + v);
			}
		});
		return list;
	}

	public boolean hasTag(ParsedProtoTag resolvedTag) {
		Preconditions.checkArgument(resolvedTag.isResolved(), "Tag must be resolved");
		if (resolvedTag.prefix().equals("*")) {
			return tags.containsKey(resolvedTag.toString());
		} else {
			return Objects.equals(tags.get(resolvedTag.key()), resolvedTag.value());
		}
	}
}
