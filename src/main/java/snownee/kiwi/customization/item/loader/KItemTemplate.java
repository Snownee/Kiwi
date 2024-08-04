package snownee.kiwi.customization.item.loader;

import java.util.Optional;
import java.util.function.Supplier;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import snownee.kiwi.customization.CustomizationRegistries;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public abstract class KItemTemplate {

	public static Codec<KItemTemplate> codec() {
		return CustomizationRegistries.ITEM_TEMPLATE.byNameCodec().dispatch(
				"type",
				KItemTemplate::type,
				type -> type.codec().get());
	}

	protected final Optional<ItemDefinitionProperties> properties;

	protected KItemTemplate(Optional<ItemDefinitionProperties> properties) {
		this.properties = properties;
	}

	public abstract Type<?> type();

	public abstract void resolve(ResourceLocation key);

	abstract Item createItem(ResourceLocation id, Item.Properties properties, JsonObject input);

	public final Optional<ItemDefinitionProperties> properties() {
		return properties;
	}

	public record Type<T extends KItemTemplate>(Supplier<Codec<T>> codec) {}
}
