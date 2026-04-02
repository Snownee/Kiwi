package snownee.kiwi.customization.item.loader;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import snownee.kiwi.customization.block.loader.InjectedCodec;
import snownee.kiwi.customization.block.tier.KiwiTiers;
import snownee.kiwi.customization.item.MultipleBlockItem;

public class ItemCodecs {
	private static final Map<Identifier, MapCodec<Item>> CODECS = Maps.newHashMap();

	public static final String ITEM_PROPERTIES_KEY = "properties";
	private static final Codec<Item.Properties> ITEM_PROPERTIES = new InjectedCodec<>(
			Codec.unit(Item.Properties::new),
			BuiltInItemTemplate.PROPERTIES_INJECTOR);

	public static <I extends Item> RecordCodecBuilder<I, Item.Properties> propertiesCodec() {
		return ITEM_PROPERTIES.fieldOf(ITEM_PROPERTIES_KEY).forGetter(item -> {
			throw new UnsupportedOperationException();
		});
	}

	public static <I extends Item> MapCodec<I> simpleCodec(Function<Item.Properties, I> function) {
		return RecordCodecBuilder.mapCodec(instance -> instance.group(propertiesCodec()).apply(instance, function));
	}

	public static <I extends TieredItem> MapCodec<I> tieredItemCodec(BiFunction<Tier, Item.Properties, I> function) {
		return RecordCodecBuilder.mapCodec(instance -> instance.group(
				KiwiTiers.CODEC.fieldOf("tier").forGetter(TieredItem::getTier),
				propertiesCodec()
		).apply(instance, function));
	}

	public static final Function<Item.Properties, Item> SIMPLE_ITEM_FACTORY = Item::new;

	public static final MapCodec<Item> ITEM = simpleCodec(SIMPLE_ITEM_FACTORY);

	static {
		register(Identifier.withDefaultNamespace("item"), ITEM);
		register(Identifier.withDefaultNamespace("blocks"), MultipleBlockItem.CODEC);
		register(Identifier.withDefaultNamespace("axe"), tieredItemCodec(AxeItem::new));
		register(Identifier.withDefaultNamespace("hoe"), tieredItemCodec(HoeItem::new));
		register(Identifier.withDefaultNamespace("pickaxe"), tieredItemCodec(PickaxeItem::new));
		register(Identifier.withDefaultNamespace("shovel"), tieredItemCodec(ShovelItem::new));
		register(Identifier.withDefaultNamespace("sword"), tieredItemCodec(SwordItem::new));
	}

	public static void register(Identifier key, MapCodec<? extends Item> codec) {
		//noinspection unchecked
		CODECS.put(key, (MapCodec<Item>) codec);
	}

	public static MapCodec<Item> get(Identifier key) {
		return Objects.requireNonNull(CODECS.get(key), key::toString);
	}
}
