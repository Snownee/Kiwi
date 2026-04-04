package snownee.kiwi.recipe;

import java.util.Objects;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public final class SizedIngredient {
	public static final Codec<SizedIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					Ingredient.MAP_CODEC_NONEMPTY.forGetter(SizedIngredient::ingredient),
					ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).forGetter(SizedIngredient::count))
			.apply(instance, SizedIngredient::new));

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
		return new SizedIngredient(Ingredient.of(tag), count);
	}

	private final Ingredient ingredient;
	private final int count;
	@Nullable
	private ItemStack[] cachedStacks;

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

	public ItemStack[] getItems() {
		if (cachedStacks == null) {
			cachedStacks = Stream.of(ingredient.getItems()).map(s -> s.copyWithCount(count)).toArray(ItemStack[]::new);
		}
		return cachedStacks;
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
}