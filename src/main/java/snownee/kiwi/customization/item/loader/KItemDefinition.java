package snownee.kiwi.customization.item.loader;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import snownee.kiwi.customization.item.KItemSettings;

public record KItemDefinition(ConfiguredItemTemplate template, ItemDefinitionProperties properties) {
	public KItemDefinition(ConfiguredItemTemplate template, ItemDefinitionProperties properties) {
		this.template = template;
		this.properties = template.template().properties().map(properties::merge).orElse(properties);
	}

	public static Codec<KItemDefinition> codec(Map<Identifier, KItemTemplate> templates) {
		KItemTemplate defaultTemplate = templates.get(Identifier.withDefaultNamespace("item"));
		Preconditions.checkNotNull(defaultTemplate);
		ConfiguredItemTemplate defaultConfiguredTemplate = new ConfiguredItemTemplate(defaultTemplate);
		return RecordCodecBuilder.create(instance -> instance.group(
				ConfiguredItemTemplate.codec(templates)
						.optionalFieldOf("template", defaultConfiguredTemplate)
						.forGetter(KItemDefinition::template),
				ItemDefinitionProperties.mapCodec().forGetter(KItemDefinition::properties)
		).apply(instance, KItemDefinition::new));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public KItemSettings.Builder createSettings(ResourceKey<Item> key) {
		KItemSettings.Builder builder = KItemSettings.builder(key);
		ItemDefinitionProperties.PartialVanillaProperties vanilla = properties.vanillaProperties();
		builder.configure($ -> {
			vanilla.maxStackSize().ifPresent($::stacksTo);
			vanilla.maxDamage().ifPresent($::durability);
			vanilla.craftingRemainingItem().map(BuiltInRegistries.ITEM::getValue).ifPresent($::craftRemainder);
			vanilla.components().ifPresent(componentMap -> {
				for (TypedDataComponent component : componentMap) {
					$.component(component.type(), component.value());
				}
			});
		});
		return builder;
	}

	public Item createItem(ResourceKey<Item> key) {
		KItemSettings.Builder builder = createSettings(key);
		return template.template().createItem(key, builder.get(), template.json());
	}
}