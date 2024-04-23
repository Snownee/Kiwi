package snownee.kiwi.customization.item;

import java.util.Map;

import com.google.common.base.Preconditions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import snownee.kiwi.customization.item.loader.ConfiguredItemTemplate;
import snownee.kiwi.customization.item.loader.ItemDefinitionProperties;
import snownee.kiwi.customization.item.loader.KItemDefinition;
import snownee.kiwi.customization.item.loader.KItemTemplate;
import snownee.kiwi.util.resource.OneTimeLoader;

public record ItemFundamentals(
		Map<ResourceLocation, KItemTemplate> templates,
		Map<ResourceLocation, KItemDefinition> items,
		ConfiguredItemTemplate blockItemTemplate,
		ItemDefinitionProperties defaultProperties) {
	public static ItemFundamentals reload(ResourceManager resourceManager, boolean booting) {
		var templates = OneTimeLoader.load(resourceManager, "kiwi/template/item", KItemTemplate.codec());
		if (booting) {
			templates.forEach((key, value) -> value.resolve(key));
		}
		var items = OneTimeLoader.load(
				resourceManager,
				"kiwi/item",
				KItemDefinition.codec(templates));
		var blockItemTemplate = templates.get(new ResourceLocation("block"));
		Preconditions.checkNotNull(blockItemTemplate, "Default block item template not found");
		return new ItemFundamentals(
				templates,
				items,
				new ConfiguredItemTemplate(blockItemTemplate, ConfiguredItemTemplate.DEFAULT_JSON),
				ItemDefinitionProperties.empty());
	}

	public void addDefaultBlockItem(ResourceLocation id) {
		items.put(id, new KItemDefinition(blockItemTemplate, defaultProperties));
	}
}
