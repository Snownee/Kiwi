package snownee.kiwi.customization.block.family;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import snownee.kiwi.util.codec.CustomizationCodecs;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class BlockFamily {
	public static final Codec<BlockFamily> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("strict", false).forGetter($ -> true),
			ResourceKey.codec(Registries.BLOCK).listOf()
					.optionalFieldOf("blocks", List.of())
					.forGetter($ -> $.blockHolders().stream().map(Holder.Reference::key).toList()),
			ResourceKey.codec(Registries.ITEM).listOf()
					.optionalFieldOf("items", List.of())
					.forGetter($ -> $.itemHolders().stream().map(Holder.Reference::key).toList()),
			CustomizationCodecs.compactList(ResourceKey.codec(Registries.ITEM))
					.optionalFieldOf("exchange_inputs_in_viewer", List.of())
					.forGetter($ -> $.exchangeInputsInViewer().stream().map(Holder.Reference::key).toList()),
			Codec.BOOL.optionalFieldOf("stonecutter_exchange", false).forGetter(BlockFamily::stonecutterExchange),
			ResourceKey.codec(Registries.ITEM)
					.optionalFieldOf("stonecutter_from")
					.forGetter($ -> $.stonecutterSource().map(Holder.Reference::key)),
			Codec.intRange(1, 64).optionalFieldOf("stonecutter_from_multiplier", 1).forGetter(BlockFamily::stonecutterSourceMultiplier),
			SwitchAttrs.CODEC.optionalFieldOf("switch", SwitchAttrs.DISABLED).forGetter(BlockFamily::switchAttrs)
	).apply(instance, BlockFamily::new));

	private final List<Holder.Reference<Block>> blocks;
	private final List<Holder.Reference<Item>> items;
	private final List<Holder.Reference<Item>> exchangeInputsInViewer;
	private final boolean stonecutterExchange;
	private final Optional<Holder.Reference<Item>> stonecutterFrom;
	private final int stonecutterFromMultiplier;
	private final SwitchAttrs switchAttrs;
	private Ingredient ingredient;
	private Ingredient ingredientInViewer;

	public BlockFamily(
			boolean strict,
			List<ResourceKey<Block>> blocks,
			List<ResourceKey<Item>> items,
			List<ResourceKey<Item>> exchangeInputsInViewer,
			boolean stonecutterExchange,
			Optional<ResourceKey<Item>> stonecutterFrom,
			int stonecutterFromMultiplier,
			SwitchAttrs switchAttrs) {
		this.blocks = blocks.stream().map($ -> {
			Optional<Holder.Reference<Block>> holder = BuiltInRegistries.BLOCK.getHolder($);
			if (strict) {
				Preconditions.checkArgument(holder.isPresent(), "Block %s not found", $);
			}
			return holder;
		}).filter(Optional::isPresent).map(Optional::get).toList();
		this.items = Stream.concat(this.blocks.stream()
				.map(Holder::value)
				.map(ItemLike::asItem)
				.filter(Predicate.not(Items.AIR::equals))
				.mapToInt(BuiltInRegistries.ITEM::getId)
				.distinct()
				.mapToObj(BuiltInRegistries.ITEM::getHolder)
				.map(Optional::orElseThrow), items.stream().map($ -> {
			Optional<Holder.Reference<Item>> holder = BuiltInRegistries.ITEM.getHolder($);
			if (strict) {
				Preconditions.checkArgument(holder.isPresent(), "Item %s not found", $);
			}
			return holder;
		}).filter(Optional::isPresent).map(Optional::get)).toList();
		this.exchangeInputsInViewer = exchangeInputsInViewer.stream().map($ -> {
			Optional<Holder.Reference<Item>> holder = BuiltInRegistries.ITEM.getHolder($);
			if (strict) {
				Preconditions.checkArgument(holder.isPresent(), "Item %s not found", $);
			}
			return holder;
		}).filter(Optional::isPresent).map(Optional::get).toList();
		this.stonecutterExchange = stonecutterExchange;
		this.stonecutterFrom = stonecutterFrom.map($ -> {
			Optional<Holder.Reference<Item>> holder = BuiltInRegistries.ITEM.getHolder($);
			if (strict) {
				Preconditions.checkArgument(holder.isPresent(), "Item %s not found", $);
			}
			return holder;
		}).filter(Optional::isPresent).map(Optional::get);
		this.stonecutterFromMultiplier = stonecutterFromMultiplier;
		this.switchAttrs = switchAttrs;
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

	public List<Holder.Reference<Block>> blockHolders() {
		return blocks;
	}

	public List<Holder.Reference<Item>> itemHolders() {
		return items;
	}

	public List<Holder.Reference<Item>> exchangeInputsInViewer() {
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

	public Optional<Holder.Reference<Item>> stonecutterSource() {
		return stonecutterFrom;
	}

	public int stonecutterSourceMultiplier() {
		return stonecutterFromMultiplier;
	}

	public Ingredient stonecutterSourceIngredient() {
		return stonecutterFrom.map(holder -> Ingredient.of(holder.value())).orElse(Ingredient.EMPTY);
	}

	public SwitchAttrs switchAttrs() {
		return switchAttrs;
	}

	protected Ingredient toIngredient(List<? extends Holder<Item>> items) {
		return Ingredient.of(items.stream().map(Holder::value).filter(item -> {
			return BlockFamilies.getConvertRatio(item) >= 1;
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

	@Override
	public String toString() {
		return "BlockFamily{" +
				"blocks=" + blocks +
				", stonecutterFrom=" + stonecutterFrom +
				'}';
	}

	public record SwitchAttrs(boolean enabled, boolean cascading, boolean creativeOnly) {
		public static final Codec<SwitchAttrs> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.BOOL.optionalFieldOf("enabled", true).forGetter(SwitchAttrs::enabled),
				Codec.BOOL.optionalFieldOf("cascading", false).forGetter(SwitchAttrs::cascading),
				Codec.BOOL.optionalFieldOf("creative_only", false).forGetter(SwitchAttrs::creativeOnly)
		).apply(instance, SwitchAttrs::create));

		public static final SwitchAttrs DISABLED = new SwitchAttrs(false, false, false);

		private static final Interner<SwitchAttrs> INTERNER = Interners.newStrongInterner();

		public static SwitchAttrs create(boolean enabled, boolean cascading, boolean creativeOnly) {
			if (!enabled) {
				Preconditions.checkArgument(!cascading, "Cascading switch must be disabled if switch is disabled");
				Preconditions.checkArgument(!creativeOnly, "Creative only switch must be disabled if switch is disabled");
				return DISABLED;
			}
			return INTERNER.intern(new SwitchAttrs(true, cascading, creativeOnly));
		}
	}
}
