package snownee.kiwi.customization;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import snownee.kiwi.Kiwi;
import snownee.kiwi.customization.block.component.KBlockComponent;
import snownee.kiwi.customization.block.loader.KBlockTemplate;
import snownee.kiwi.customization.item.loader.KItemTemplate;

public class CustomizationRegistries {
	public static final ResourceKey<Registry<KBlockComponent.Type<?>>> BLOCK_COMPONENT_KEY = ResourceKey.createRegistryKey(Kiwi.id(
			"block_component"));
	public static Registry<KBlockComponent.Type<?>> BLOCK_COMPONENT;
	public static final ResourceKey<Registry<KBlockTemplate.Type<?>>> BLOCK_TEMPLATE_KEY = ResourceKey.createRegistryKey(Kiwi.id(
			"block_template"));
	public static Registry<KBlockTemplate.Type<?>> BLOCK_TEMPLATE;
	public static final ResourceKey<Registry<KItemTemplate.Type<?>>> ITEM_TEMPLATE_KEY = ResourceKey.createRegistryKey(Kiwi.id(
			"item_template"));
	public static Registry<KItemTemplate.Type<?>> ITEM_TEMPLATE;
}
