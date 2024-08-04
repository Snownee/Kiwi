package snownee.kiwi.customization.item;

import java.util.function.Consumer;

import net.minecraft.world.item.Item;

public class KItemSettings {

	private KItemSettings(Builder builder) {
	}

	public static KItemSettings empty() {
		return new KItemSettings(builder());
	}

	public static Builder builder() {
		return new Builder(new Item.Properties());
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
