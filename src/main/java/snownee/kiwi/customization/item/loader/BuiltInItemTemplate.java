package snownee.kiwi.customization.item.loader;

import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import snownee.kiwi.util.resource.OneTimeLoader;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class BuiltInItemTemplate extends KItemTemplate {
	public static final ThreadLocal<Item.Properties> PROPERTIES_INJECTOR = new ThreadLocal<>();
	private final Optional<Identifier> key;
	private @Nullable MapCodec<Item> codec;

	public BuiltInItemTemplate(Optional<ItemDefinitionProperties> properties, Optional<Identifier> key) {
		super(properties);
		this.key = key;
	}

	public static MapCodec<BuiltInItemTemplate> directCodec() {
		return RecordCodecBuilder.mapCodec(instance -> instance.group(
				ItemDefinitionProperties.mapCodecField().forGetter(BuiltInItemTemplate::properties),
				Identifier.CODEC.optionalFieldOf("codec").forGetter(BuiltInItemTemplate::key)
		).apply(instance, BuiltInItemTemplate::new));
	}

	@Override
	public Type<?> type() {
		return KItemTemplates.BUILT_IN.getOrCreate();
	}

	@Override
	public void resolve(Identifier key, OneTimeLoader.Context context) {
		codec = ItemCodecs.get(this.key.orElse(key));
	}

	@Override
	public Item createItem(ResourceKey<Item> key, Item.Properties properties, JsonObject json) {
		if (!json.has(ItemCodecs.ITEM_PROPERTIES_KEY)) {
			json.add(ItemCodecs.ITEM_PROPERTIES_KEY, new JsonObject());
		}
		PROPERTIES_INJECTOR.set(properties);
		DataResult<Item> result = Objects.requireNonNull(codec).decode(
				JsonOps.INSTANCE,
				JsonOps.INSTANCE.getMap(json).result().orElseThrow());
		if (result.error().isPresent()) {
			throw new IllegalStateException(result.error().get().message());
		}
		return result.result().orElseThrow();
	}

	public Optional<Identifier> key() {
		return key;
	}

	@Override
	public String toString() {
		return "BuiltInItemTemplate[" + "properties=" + properties + ", " + "key=" + key + ", " + "codec=" + codec + ']';
	}

}