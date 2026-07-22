package snownee.kiwi.recipe;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class AlternativesIngredientBuilder implements CustomIngredient {
	private HolderLookup.@Nullable Provider registries;
	private List<JsonElement> options = Lists.newArrayList();
	private boolean allowEmpty;

	public static AlternativesIngredientBuilder of(HolderLookup.Provider registries) {
		return new AlternativesIngredientBuilder(registries);
	}

	public AlternativesIngredientBuilder(List<JsonElement> options) {
		this.options = Lists.newArrayList(options);
	}

	public AlternativesIngredientBuilder(HolderLookup.Provider registries) {
		this.registries = registries;
	}

	public AlternativesIngredientBuilder add(Ingredient ingredient) {
		if (allowEmpty) {
			throw new IllegalStateException("Cannot add options after allowEmpty() has been called");
		}
		RegistryOps<JsonElement> ops = Objects.requireNonNull(registries).createSerializationContext(JsonOps.INSTANCE);
		options.add(Ingredient.CODEC.encodeStart(ops, ingredient).result().orElseThrow());
		return this;
	}

	public AlternativesIngredientBuilder add(ItemLike itemLike) {
		add(Ingredient.of(itemLike));
		return this;
	}

	public AlternativesIngredientBuilder add(TagKey<Item> tag) {
		add(RecipeUtil.tagIngredient(Objects.requireNonNull(registries).lookupOrThrow(Registries.ITEM), tag));
		return this;
	}

	public AlternativesIngredientBuilder add(CustomIngredient ingredient) {
		add(ingredient.toVanilla());
		return this;
	}

	public AlternativesIngredientBuilder add(String tagOrItem) {
		if (tagOrItem.startsWith("#")) {
			add(TagKey.create(Registries.ITEM, Identifier.parse(tagOrItem.substring(1))));
		} else {
			Item item = Objects.requireNonNull(registries)
					.getOrThrow(ResourceKey.create(Registries.ITEM, Identifier.parse(tagOrItem)))
					.value();
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
		options.add(new JsonArray());
		return this;
	}

	@Override
	public boolean test(ItemStack stack) {
		return false;
	}

	@Override
	public Stream<Holder<Item>> items() {
		return Stream.empty();
	}

	@Override
	public boolean requiresTesting() {
		return false;
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return Serializer.INSTANCE;
	}

	public enum Serializer implements CustomIngredientSerializer<AlternativesIngredientBuilder> {
		INSTANCE;

		public static final MapCodec<AlternativesIngredientBuilder> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				Codec.list(ExtraCodecs.JSON).fieldOf("options").forGetter(o -> o.options)
		).apply(i, AlternativesIngredientBuilder::new));

		@Override
		public Identifier getIdentifier() {
			return AlternativesIngredient.ID;
		}

		@Override
		public MapCodec<AlternativesIngredientBuilder> getCodec() {
			return CODEC;
		}

		@SuppressWarnings("DataFlowIssue")
		@Override
		public StreamCodec<RegistryFriendlyByteBuf, AlternativesIngredientBuilder> getStreamCodec() {
			// Builder ingredients are data-generation only
			return StreamCodec.unit(null);
		}
	}
}
