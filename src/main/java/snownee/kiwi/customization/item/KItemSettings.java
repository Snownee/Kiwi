package snownee.kiwi.customization.item;

import java.util.function.Consumer;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class KItemSettings {

	private KItemSettings(Builder builder) {
	}

	public static KItemSettings defaulted(Item item) {
		return new KItemSettings(builder(BuiltInRegistries.ITEM.getResourceKey(item).orElseThrow()));
	}

	public static Builder builder(ResourceKey<Item> key) {
		return new Builder(new Item.Properties().setId(key));
	}

	public static class Builder {
		private final Item.Properties properties;

		private Builder(Item.Properties properties) {
			this.properties = properties;
		}

		public Item.Properties get() {
			return properties;
		}

		public Builder configure(Consumer<Item.Properties> configurator) {
			configurator.accept(properties);
			return this;
		}
	}
}
