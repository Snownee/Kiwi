package snownee.kiwi.customization.item.loader;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import snownee.kiwi.customization.item.KItemSettings;

public record KItemDefinition(ConfiguredItemTemplate template, ItemDefinitionProperties properties) {
	public KItemDefinition(ConfiguredItemTemplate template, ItemDefinitionProperties properties) {
		this.template = template;
		this.properties = template.template().properties().map(properties::merge).orElse(properties);
	}

	public static Codec<KItemDefinition> codec(Map<ResourceLocation, KItemTemplate> templates) {
		KItemTemplate defaultTemplate = templates.get(ResourceLocation.withDefaultNamespace("item"));
		Preconditions.checkNotNull(defaultTemplate);
		ConfiguredItemTemplate defaultConfiguredTemplate = new ConfiguredItemTemplate(defaultTemplate);
		return RecordCodecBuilder.create(instance -> instance.group(
				ConfiguredItemTemplate.codec(templates)
						.optionalFieldOf("template", defaultConfiguredTemplate)
						.forGetter(KItemDefinition::template),
				ItemDefinitionProperties.mapCodec().forGetter(KItemDefinition::properties)
		).apply(instance, KItemDefinition::new));
	}

	public KItemSettings.Builder createSettings(ResourceLocation id) {
		KItemSettings.Builder builder = KItemSettings.builder();
		ItemDefinitionProperties.PartialVanillaProperties vanilla = properties.vanillaProperties();
		builder.configure($ -> {
			vanilla.maxStackSize().ifPresent($::stacksTo);
			vanilla.maxDamage().ifPresent($::durability);
			vanilla.craftingRemainingItem().map(BuiltInRegistries.ITEM::get).ifPresent($::craftRemainder);
			vanilla.food().ifPresent($::food);
			vanilla.rarity().ifPresent($::rarity);
		});
		return builder;
	}

	public Item createItem(ResourceLocation id) {
		KItemSettings.Builder builder = createSettings(id);
		return template.template().createItem(id, builder.get(), template.json());
	}
}
