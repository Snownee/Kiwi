package snownee.kiwi.customization.item.loader;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Function4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.ToolMaterial;
import snownee.kiwi.customization.block.loader.InjectedCodec;
import snownee.kiwi.customization.item.MultipleBlockItem;
import snownee.kiwi.customization.item.toolmaterial.ToolMaterials;
import snownee.kiwi.util.codec.KCodecs;

public class ItemCodecs {
	private static final Map<Identifier, MapCodec<Item>> CODECS = Maps.newHashMap();

	public static final String ITEM_PROPERTIES_KEY = "properties";
	private static final Codec<Item.Properties> ITEM_PROPERTIES = new InjectedCodec<>(
			MapCodec.unitCodec(Item.Properties::new),
			BuiltInItemTemplate.PROPERTIES_INJECTOR);

	public static <I extends Item> RecordCodecBuilder<I, Item.Properties> propertiesCodec() {
		return ITEM_PROPERTIES.fieldOf(ITEM_PROPERTIES_KEY).forGetter(KCodecs.unsupportedGetter());
	}

	public static <I extends Item> MapCodec<I> simpleCodec(Function<Item.Properties, I> function) {
		return RecordCodecBuilder.mapCodec(instance -> instance.group(propertiesCodec()).apply(instance, function));
	}

	public static <I extends Item> MapCodec<I> toolCodec(Function4<ToolMaterial, Float, Float, Item.Properties, I> function) {
		return RecordCodecBuilder.mapCodec(instance -> instance.group(
				ToolMaterials.CODEC.fieldOf("tier").forGetter(KCodecs.unsupportedGetter()),
				Codec.FLOAT.fieldOf("attack_damage").forGetter(KCodecs.unsupportedGetter()),
				Codec.FLOAT.fieldOf("attack_speed").forGetter(KCodecs.unsupportedGetter()),
				propertiesCodec()
		).apply(instance, function));
	}

	public static final Function<Item.Properties, Item> SIMPLE_ITEM_FACTORY = Item::new;

	static {
		register(Identifier.withDefaultNamespace("item"), simpleCodec(SIMPLE_ITEM_FACTORY));
		register(Identifier.withDefaultNamespace("blocks"), MultipleBlockItem.CODEC);
		register(Identifier.withDefaultNamespace("axe"), toolCodec(AxeItem::new));
		register(Identifier.withDefaultNamespace("hoe"), toolCodec(HoeItem::new));
		register(Identifier.withDefaultNamespace("shovel"), toolCodec(ShovelItem::new));
		register(
				Identifier.withDefaultNamespace("pickaxe"),
				toolCodec((mat, damage, speed, properties) -> new Item(properties.pickaxe(mat, damage, speed))));
		register(
				Identifier.withDefaultNamespace("sword"),
				toolCodec((mat, damage, speed, properties) -> new Item(properties.sword(mat, damage, speed))));
	}

	public static void register(Identifier key, MapCodec<? extends Item> codec) {
		//noinspection unchecked
		CODECS.put(key, (MapCodec<Item>) codec);
	}

	public static MapCodec<Item> get(Identifier key) {
		return Objects.requireNonNull(CODECS.get(key), key::toString);
	}
}
