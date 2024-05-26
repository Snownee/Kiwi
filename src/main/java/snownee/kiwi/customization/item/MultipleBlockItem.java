package snownee.kiwi.customization.item;

import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import snownee.kiwi.customization.item.loader.ItemCodecs;
import snownee.kiwi.util.NotNullByDefault;

@NotNullByDefault
public class MultipleBlockItem extends BlockItem {
	public static final MapCodec<MultipleBlockItem> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.compoundList(Codec.STRING, BuiltInRegistries.BLOCK.byNameCodec()).fieldOf("blocks").forGetter($ -> {
				throw new UnsupportedOperationException();
			}),
			ItemCodecs.propertiesCodec()
	).apply(instance, MultipleBlockItem::new));

	private final List<Pair<String, Block>> blocks;

	public MultipleBlockItem(List<Pair<String, Block>> blocks, Properties properties) {
		super(blocks.get(0).getSecond(), properties);
		this.blocks = blocks;
		Preconditions.checkArgument(blocks.size() > 1, "MultipleBlockItem must have more than one block");
		Preconditions.checkArgument(
				blocks.stream().map(Pair::getSecond).allMatch(block -> block != Blocks.AIR),
				"MultipleBlockItem cannot have AIR block");
	}

	@Override
	public void registerBlocks(Map<Block, Item> pBlockToItemMap, Item pItem) {
		blocks.stream().map(Pair::getSecond).forEach(block -> pBlockToItemMap.put(block, pItem));
	}

	@Override
	public void removeFromBlockToItemMap(Map<Block, Item> blockToItemMap, Item itemIn) {
		blocks.stream().map(Pair::getSecond).forEach(blockToItemMap::remove);
	}

	public Block getBlock(String name) {
		for (Pair<String, Block> pair : blocks) {
			if (pair.getFirst().equals(name)) {
				return pair.getSecond();
			}
		}
		return Blocks.AIR;
	}
}
