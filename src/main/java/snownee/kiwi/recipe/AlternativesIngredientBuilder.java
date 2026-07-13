package snownee.kiwi.recipe;

import java.util.List;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;

public class AlternativesIngredientBuilder implements CustomIngredient {
	private final @Nullable HolderGetter<Item> lookup;
	private final List<@Nullable Ingredient> ingredients = Lists.newArrayList();
	private boolean allowEmpty;

	public AlternativesIngredientBuilder(HolderGetter<Item> lookup) {
		this.lookup = lookup;
	}

	private AlternativesIngredientBuilder(List<@Nullable Ingredient> ingredients) {
		this.lookup = null;
		this.ingredients.addAll(ingredients);
		this.allowEmpty = ingredients.contains(null);
	}

	public static AlternativesIngredientBuilder of(HolderGetter<Item> lookup) {
		return new AlternativesIngredientBuilder(lookup);
	}

	public AlternativesIngredientBuilder add(Ingredient ingredient) {
		if (allowEmpty) {
			throw new IllegalStateException("Cannot add options after allowEmpty() has been called");
		}
		ingredients.add(ingredient);
		return this;
	}

	public AlternativesIngredientBuilder add(ItemLike itemLike) {
		return add(Ingredient.of(itemLike));
	}

	public AlternativesIngredientBuilder add(TagKey<Item> tag) {
		if (lookup == null) {
			throw new IllegalStateException("Tag options require a registry lookup");
		}
		return add(RecipeUtil.tagIngredient(lookup, tag));
	}

	public AlternativesIngredientBuilder add(ICustomIngredient ingredient) {
		return add(ingredient.toVanilla());
	}

	public AlternativesIngredientBuilder add(CustomIngredient ingredient) {
		return add(ingredient.toVanilla());
	}

	public AlternativesIngredientBuilder add(String tagOrItem) {
		if (lookup == null) {
			throw new IllegalStateException("String options require a registry lookup");
		}
		if (tagOrItem.startsWith("#")) {
			add(TagKey.create(Registries.ITEM, Identifier.parse(tagOrItem.substring(1))));
		} else {
			Item item = lookup.getOrThrow(ResourceKey.create(Registries.ITEM, Identifier.parse(tagOrItem))).value();
			Preconditions.checkState(item != Items.AIR);
			add(item);
		}
		return this;
	}

	public AlternativesIngredientBuilder allowEmpty() {
		if (allowEmpty) {
			throw new IllegalStateException("allowEmpty() has already been called");
		}
		allowEmpty = true;
		ingredients.add(null);
		return this;
	}

	public AlternativesIngredient build() {
		return new AlternativesIngredient(ingredients);
	}

	@Override
	public boolean test(ItemStack stack) {
		return false;
	}

	@Override
	public List<ItemStack> getMatchingStacks() {
		return List.of();
	}

	@Override
	public boolean requiresTesting() {
		return false;
	}

	@Override
	public SlotDisplay display() {
		return SlotDisplay.Empty.INSTANCE;
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return Serializer.INSTANCE;
	}

	public static class Serializer extends MapCodec<AlternativesIngredientBuilder> implements CustomIngredientSerializer<AlternativesIngredientBuilder> {
		public static final Serializer INSTANCE = new Serializer();

		@Override
		public Identifier getIdentifier() {
			return AlternativesIngredient.ID;
		}

		@Override
		public MapCodec<AlternativesIngredientBuilder> getCodec(boolean allowEmpty) {
			return this;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, AlternativesIngredientBuilder> getPacketCodec() {
			throw new UnsupportedOperationException("Builder ingredients are data-generation only");
		}

		@Override
		public <T> Stream<T> keys(DynamicOps<T> ops) {
			return Stream.of(ops.createString("options"));
		}

		@Override
		public <T> DataResult<AlternativesIngredientBuilder> decode(DynamicOps<T> ops, MapLike<T> input) {
			return AlternativesIngredient.Serializer.decodeOptions(ops, input).map(AlternativesIngredientBuilder::new);
		}

		@Override
		public <T> RecordBuilder<T> encode(AlternativesIngredientBuilder input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
			return prefix.add("options", ops.createList(input.ingredients.stream().map(ingredient -> ingredient == null ?
					ops.emptyList() : Ingredient.CODEC.encodeStart(ops, ingredient).getOrThrow())));
		}
	}
}
