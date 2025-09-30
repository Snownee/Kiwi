package snownee.kiwi.customization.item.loader;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import snownee.kiwi.customization.item.MultipleBlockItem;

public class ItemCodecs {
	private static final Map<ResourceLocation, MapCodec<Item>> CODECS = Maps.newHashMap();

	public static final String ITEM_PROPERTIES_KEY = "properties";
	private static final Codec<Item.Properties> ITEM_PROPERTIES = Codec.unit(Item.Properties::new);

	public static <I extends Item> RecordCodecBuilder<I, Item.Properties> propertiesCodec() {
		return ITEM_PROPERTIES.fieldOf(ITEM_PROPERTIES_KEY).forGetter(item -> {
			throw new UnsupportedOperationException();
		});
	}

	public static <I extends Item> MapCodec<I> simpleCodec(Function<Item.Properties, I> function) {
		return RecordCodecBuilder.mapCodec(instance -> instance.group(propertiesCodec()).apply(instance, function));
	}

	public static final Function<Item.Properties, Item> SIMPLE_ITEM_FACTORY = Item::new;

	public static final MapCodec<Item> ITEM = simpleCodec(SIMPLE_ITEM_FACTORY);

	static {
		register(new ResourceLocation("item"), ITEM);
		register(new ResourceLocation("blocks"), MultipleBlockItem.CODEC);
	}

	public static void register(ResourceLocation key, MapCodec<? extends Item> codec) {
		//noinspection unchecked
		CODECS.put(key, (MapCodec<Item>) codec);
	}

	public static MapCodec<Item> get(ResourceLocation key) {
		return Objects.requireNonNull(CODECS.get(key), key::toString);
	}
}
