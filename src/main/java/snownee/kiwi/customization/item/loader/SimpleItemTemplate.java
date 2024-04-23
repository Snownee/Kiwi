package snownee.kiwi.customization.item.loader;

import java.util.Optional;
import java.util.function.Function;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class SimpleItemTemplate extends KItemTemplate {
	private final String clazz;
	private Function<Item.Properties, Item> constructor;

	public SimpleItemTemplate(Optional<ItemDefinitionProperties> properties, String clazz) {
		super(properties);
		this.clazz = clazz;
	}

	public static Codec<SimpleItemTemplate> directCodec() {
		return RecordCodecBuilder.create(instance -> instance.group(
				ItemDefinitionProperties.mapCodecField().forGetter(SimpleItemTemplate::properties),
				Codec.STRING.optionalFieldOf("class", "").forGetter(SimpleItemTemplate::clazz)
		).apply(instance, SimpleItemTemplate::new));
	}

	@Override
	public Type<?> type() {
		return KItemTemplates.SIMPLE.getOrCreate();
	}

	@Override
	public void resolve(ResourceLocation key) {
		if (clazz.isEmpty()) {
			constructor = ItemCodecs.SIMPLE_ITEM_FACTORY;
			return;
		}
		try {
			Class<?> clazz = Class.forName(this.clazz);
			this.constructor = $ -> {
				try {
					return (Item) clazz.getConstructor(Item.Properties.class).newInstance($);
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}
			};
		} catch (Throwable e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public Item createItem(ResourceLocation id, Item.Properties settings, JsonObject input) {
		return this.constructor.apply(settings);
	}

	public String clazz() {
		return clazz;
	}

	@Override
	public String toString() {
		return "SimpleItemTemplate[" + "properties=" + properties + ", " + "clazz=" + clazz + ", " + "constructor=" + constructor + ']';
	}

}
