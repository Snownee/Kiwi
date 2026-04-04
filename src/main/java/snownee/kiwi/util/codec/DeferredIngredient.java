package snownee.kiwi.util.codec;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import net.minecraft.core.registries.Registries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import snownee.kiwi.mixin.customization.Ingredient_TagValueAccess;
import snownee.kiwi.util.DeferredHolder;

public class DeferredIngredient implements Supplier<Ingredient> {
	public static final Codec<Ingredient.Value> VALUE_CODEC =
			Codec.xor(DeferredItemValue.CODEC, Ingredient_TagValueAccess.getCODEC())
					.xmap(
							(it) -> it.map(Function.identity(), Function.identity()),
							(it) -> {
								if (it instanceof Ingredient.TagValue tagValue) {
									return Either.right(tagValue);
								} else if (it instanceof DeferredItemValue itemValue) {
									return Either.left(itemValue);
								} else {
									throw new UnsupportedOperationException("This is neither an item value nor a tag value.");
								}
							});
	public static final Codec<DeferredIngredient> CODEC =
			ExtraCodecs.compactListCodec(VALUE_CODEC).xmap(DeferredIngredient::new, it -> it.values);

	private Ingredient resolved;
	private final List<Ingredient.Value> values;

	public DeferredIngredient(List<Ingredient.Value> values) {
		this.values = values;
	}

	@Override
	public Ingredient get() {
		if (resolved == null) {
			resolved = Ingredient.fromValues(values.stream());
		}
		return resolved;
	}

	public record DeferredItemValue(DeferredHolder<Item, Item> item) implements Ingredient.Value {
		public static final Codec<DeferredItemValue> CODEC =
				DeferredHolder.codec(Registries.ITEM)
						.xmap(DeferredItemValue::new, DeferredItemValue::item);

		@Override
		public Collection<ItemStack> getItems() {
			return List.of(item.get().getDefaultInstance());
		}
	}
}
