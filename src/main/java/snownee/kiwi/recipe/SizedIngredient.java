package snownee.kiwi.recipe;

import java.util.Objects;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.ItemLike;
import snownee.kiwi.data.DataModule;

public final class SizedIngredient {
	public static final SizedIngredient EMPTY = new SizedIngredient(RecipeUtil.emptyIngredient(), 1);

	private static final Codec<SizedIngredient> FLAT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Ingredient.NON_AIR_HOLDER_SET_CODEC.xmap(Ingredient::of, Ingredient::getValues)
					.fieldOf("ingredient")
					.forGetter(SizedIngredient::ingredient),
			ExtraCodecs.POSITIVE_INT.fieldOf("count").forGetter(SizedIngredient::count)
	).apply(instance, SizedIngredient::new));

	public static final Codec<SizedIngredient> CODEC = Codec.withAlternative(
			RecordCodecBuilder.create(instance -> instance.group(
					Ingredient.CODEC.fieldOf("ingredient").forGetter(SizedIngredient::ingredient),
					ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).forGetter(SizedIngredient::count)
			).apply(instance, SizedIngredient::new)), FLAT_CODEC);

	public static final StreamCodec<RegistryFriendlyByteBuf, SizedIngredient> STREAM_CODEC = StreamCodec.composite(
			Ingredient.CONTENTS_STREAM_CODEC,
			SizedIngredient::ingredient,
			ByteBufCodecs.VAR_INT,
			SizedIngredient::count,
			SizedIngredient::new);

	public static SizedIngredient of(ItemLike item, int count) {
		return new SizedIngredient(Ingredient.of(item), count);
	}

	public static SizedIngredient of(TagKey<Item> tag, int count) {
		return new SizedIngredient(RecipeUtil.tagIngredient(tag), count);
	}

	private final Ingredient ingredient;
	private final int count;

	public SizedIngredient(Ingredient ingredient, int count) {
		Preconditions.checkArgument(count > 0, "Count must be positive");
		this.ingredient = ingredient;
		this.count = count;
	}

	public Ingredient ingredient() {
		return ingredient;
	}

	public int count() {
		return count;
	}

	public boolean test(ItemStack stack) {
		return ingredient.test(stack) && (stack.isEmpty() || stack.getCount() >= count);
	}

	public SlotDisplay display() {
		if (count == 1) {
			return ingredient.display();
		}
		return new SizedSlotDisplay(ingredient.display(), count);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof SizedIngredient other)) {
			return false;
		}
		return count == other.count && ingredient.equals(other.ingredient);
	}

	@Override
	public int hashCode() {
		return Objects.hash(ingredient, count);
	}

	@Override
	public String toString() {
		return count + "x " + ingredient;
	}

	public record SizedSlotDisplay(SlotDisplay display, int count) implements SlotDisplay {
		public static final MapCodec<SizedSlotDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				SlotDisplay.CODEC.fieldOf(
						"display").forGetter(SizedSlotDisplay::display),
				ExtraCodecs.POSITIVE_INT.fieldOf("count").forGetter(SizedSlotDisplay::count)).apply(i, SizedSlotDisplay::new));
		public static final StreamCodec<RegistryFriendlyByteBuf, SizedSlotDisplay> STREAM_CODEC = StreamCodec.composite(
				SlotDisplay.STREAM_CODEC,
				SizedSlotDisplay::display,
				ByteBufCodecs.VAR_INT,
				SizedSlotDisplay::count,
				SizedSlotDisplay::new);

		@Override
		public String toString() {
			return count + "x " + display;
		}

		@Override
		public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> output) {
			return display.resolve(context, output).peek($ -> {
				if ($ instanceof ItemStack itemStack) {
					itemStack.setCount(count);
				}
			});
		}

		@Override
		public ItemStack resolveForFirstStack(ContextMap context) {
			return display.resolveForFirstStack(context).copyWithCount(count);
		}

		@Override
		public boolean isEnabled(FeatureFlagSet enabledFeatures) {
			return display.isEnabled(enabledFeatures);
		}

		@Override
		public Type<? extends SlotDisplay> type() {
			return DataModule.SIZED.getOrCreate();
		}
	}
}