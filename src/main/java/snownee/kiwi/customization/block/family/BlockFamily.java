package snownee.kiwi.customization.block.family;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SlabBlock;
import snownee.kiwi.util.codec.CustomizationCodecs;

public class BlockFamily {
	public static final Codec<BlockFamily> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BuiltInRegistries.BLOCK.holderByNameCodec().listOf()
					.optionalFieldOf("blocks", List.of())
					.forGetter(BlockFamily::blockHolders),
			BuiltInRegistries.ITEM.holderByNameCodec().listOf()
					.optionalFieldOf("items", List.of())
					.forGetter(BlockFamily::itemHolders),
			CustomizationCodecs.compactList(BuiltInRegistries.ITEM.holderByNameCodec())
					.optionalFieldOf("exchange_inputs_in_viewer", List.of())
					.forGetter(BlockFamily::exchangeInputsInViewer),
			Codec.BOOL.optionalFieldOf("stonecutter_exchange", false).forGetter(BlockFamily::stonecutterExchange),
			BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("stonecutter_from", Items.AIR).forGetter(BlockFamily::stonecutterSource),
			Codec.intRange(1, 64).optionalFieldOf("stonecutter_from_multiplier", 1).forGetter(BlockFamily::stonecutterSourceMultiplier),
			Codec.BOOL.optionalFieldOf("quick_switch", false).forGetter(BlockFamily::quickSwitch),
			Codec.BOOL.optionalFieldOf("cascading_switch", false).forGetter(BlockFamily::cascadingSwitch)
	).apply(instance, BlockFamily::new));

	private final List<Holder<Block>> blocks;
	private final List<Holder<Item>> items;
	private final List<Holder<Item>> exchangeInputsInViewer;
	private final boolean stonecutterExchange;
	private final Item stonecutterFrom;
	private final int stonecutterFromMultiplier;
	private final boolean quickSwitch;
	private final boolean cascadingSwitch;
	private Ingredient ingredient;
	private Ingredient ingredientInViewer;

	public BlockFamily(
			List<Holder<Block>> blocks,
			List<Holder<Item>> items,
			List<Holder<Item>> exchangeInputsInViewer,
			boolean stonecutterExchange,
			Item stonecutterFrom,
			int stonecutterFromMultiplier,
			boolean quickSwitch,
			boolean cascadingSwitch) {
		this.blocks = blocks;
		this.items = Stream.concat(blocks.stream()
				.map(Holder::value)
				.map(ItemLike::asItem)
				.filter(Predicate.not(Items.AIR::equals))
				.mapToInt(BuiltInRegistries.ITEM::getId)
				.distinct()
				.mapToObj(BuiltInRegistries.ITEM::getHolder)
				.map(Optional::orElseThrow), items.stream()).toList();
		this.exchangeInputsInViewer = exchangeInputsInViewer;
		this.stonecutterExchange = stonecutterExchange;
		this.stonecutterFrom = stonecutterFrom;
		this.stonecutterFromMultiplier = stonecutterFromMultiplier;
		this.quickSwitch = quickSwitch;
		this.cascadingSwitch = cascadingSwitch;
		Preconditions.checkArgument(!blocks.isEmpty() || !items.isEmpty(), "No entries found in family");
		Preconditions.checkArgument(
				blocks().distinct().count() == this.blocks.size(),
				"Duplicate blocks found in family %s",
				this);
		Preconditions.checkArgument(
				items().distinct().count() == this.items.size(),
				"Duplicate items found in family %s",
				this);
	}

	public List<Holder<Block>> blockHolders() {
		return blocks;
	}

	public List<Holder<Item>> itemHolders() {
		return items;
	}

	public List<Holder<Item>> exchangeInputsInViewer() {
		return exchangeInputsInViewer;
	}

	public Stream<Block> blocks() {
		return blocks.stream().map(Holder::value);
	}

	public Stream<Item> items() {
		return items.stream().map(Holder::value);
	}

	public boolean stonecutterExchange() {
		return stonecutterExchange;
	}

	public Item stonecutterSource() {
		return stonecutterFrom;
	}

	public int stonecutterSourceMultiplier() {
		return stonecutterFromMultiplier;
	}

	public Ingredient stonecutterSourceIngredient() {
		return stonecutterFrom == Items.AIR ? Ingredient.EMPTY : Ingredient.of(stonecutterFrom);
	}

	public boolean quickSwitch() {
		return quickSwitch;
	}

	private boolean cascadingSwitch() {
		return cascadingSwitch;
	}

	protected Ingredient toIngredient(List<Holder<Item>> items) {
		return Ingredient.of(items.stream().map(Holder::value).filter(item -> {
			Block block = Block.byItem(item);
			if (block instanceof SlabBlock || block instanceof DoorBlock) {
				return false;
			}
			return true;
		}).toArray(ItemLike[]::new));
	}

	public Ingredient ingredient() {
		if (ingredient == null) {
			ingredient = toIngredient(items);
		}
		return ingredient;
	}

	public Ingredient ingredientInViewer() {
		if (ingredientInViewer == null) {
			if (exchangeInputsInViewer.isEmpty()) {
				ingredientInViewer = ingredient();
			} else {
				ingredientInViewer = toIngredient(exchangeInputsInViewer);
			}
		}
		return ingredientInViewer;
	}

	public boolean contains(Item item) {
		return items.stream().anyMatch(h -> h.value().asItem() == item);
	}
}
