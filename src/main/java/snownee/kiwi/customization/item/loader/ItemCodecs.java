package snownee.kiwi.customization.item.loader;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
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
	private static final Map<ResourceLocation, MapCodec<Item>> CODECS = Maps.newHashMap();

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

	public static final MapCodec<AxeItem> AXE_ITEM = tieredItemCodec(AxeItem::new);
	public static final MapCodec<HoeItem> HOE_ITEM = tieredItemCodec(HoeItem::new);
	public static final MapCodec<PickaxeItem> PICKAXE_ITEM = tieredItemCodec(PickaxeItem::new);
	public static final MapCodec<ShovelItem> SHOVEL_ITEM = tieredItemCodec(ShovelItem::new);
	public static final MapCodec<SwordItem> SWORD_ITEM = tieredItemCodec(SwordItem::new);

	static {
		register(ResourceLocation.withDefaultNamespace("item"), ITEM);
		register(ResourceLocation.withDefaultNamespace("blocks"), MultipleBlockItem.CODEC);
		register(ResourceLocation.withDefaultNamespace("axe"), AXE_ITEM);
		register(ResourceLocation.withDefaultNamespace("hoe"), HOE_ITEM);
		register(ResourceLocation.withDefaultNamespace("pickaxe"), PICKAXE_ITEM);
		register(ResourceLocation.withDefaultNamespace("shovel"), SHOVEL_ITEM);
		register(ResourceLocation.withDefaultNamespace("sword"), SWORD_ITEM);
	}

	public static void register(ResourceLocation key, MapCodec<? extends Item> codec) {
		//noinspection unchecked
		CODECS.put(key, (MapCodec<Item>) codec);
	}

	public static MapCodec<Item> get(ResourceLocation key) {
		return Objects.requireNonNull(CODECS.get(key), key::toString);
	}
}
