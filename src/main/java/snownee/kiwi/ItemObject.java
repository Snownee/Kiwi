package snownee.kiwi;

import java.util.Objects;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public class ItemObject<T extends Item> extends KiwiGO<T> implements ItemLike {
	private @Nullable Function<Item.Properties, T> factory;

	public ItemObject(Function<Item.Properties, T> factory) {
		super(null);
		this.factory = Objects.requireNonNull(factory);
	}

	@Override
	public T preRegister(Identifier id) {
		//noinspection unchecked
		setKey((ResourceKey<T>) ResourceKey.create(Registries.ITEM, id));
		return getOrCreate();
	}

	@Override
	public T getOrCreate() {
		if (value == null) {
			Objects.requireNonNull(factory);
			//noinspection unchecked,NullableProblems
			value = Objects.requireNonNull(factory.apply(new Item.Properties().setId((ResourceKey<Item>) resourceKey())));
			factory = null;
		}
		return get();
	}

	@Override
	public Item asItem() {
		return get();
	}

	@Override
	public ResourceKey<? extends Registry<?>> findRegistry() {
		return Registries.ITEM;
	}
}
