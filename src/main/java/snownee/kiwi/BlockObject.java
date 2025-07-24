package snownee.kiwi;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BlockObject<T extends Block> extends KiwiGO<T> implements ItemLike {
	private Function<Block.Properties, T> factory;
	private @Nullable Supplier<Block> copyFrom;

	public BlockObject(Function<Block.Properties, T> factory, @Nullable Supplier<Block> copyFrom) {
		super(null);
		this.factory = Objects.requireNonNull(factory);
		this.copyFrom = copyFrom;
	}

	@Override
	public T preRegister(ResourceLocation id) {
		//noinspection unchecked
		setKey((ResourceKey<T>) ResourceKey.create(Registries.BLOCK, id));
		return getOrCreate();
	}

	@Override
	public T getOrCreate() {
		if (value == null) {
			Objects.requireNonNull(factory);
			BlockBehaviour.Properties properties;
			if (copyFrom != null) {
				properties = BlockBehaviour.Properties.ofFullCopy(copyFrom.get());
			} else {
				properties = BlockBehaviour.Properties.of();
			}
			//noinspection unchecked
			properties.setId((ResourceKey<Block>) resourceKey());
			value = Objects.requireNonNull(factory.apply(properties));
			factory = null;
			copyFrom = null;
		}
		return get();
	}

	@Override
	public Item asItem() {
		return get().asItem();
	}

	@Override
	public ResourceKey<? extends Registry<?>> findRegistry() {
		return Registries.BLOCK;
	}
}
