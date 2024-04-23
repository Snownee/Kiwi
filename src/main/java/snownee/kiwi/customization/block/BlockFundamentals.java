package snownee.kiwi.customization.block;

import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.MapCodec;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import snownee.kiwi.customization.block.loader.KBlockDefinition;
import snownee.kiwi.customization.block.loader.KBlockTemplate;
import snownee.kiwi.customization.block.loader.KMaterial;
import snownee.kiwi.util.codec.CustomizationCodecs;
import snownee.kiwi.customization.placement.PlaceChoices;
import snownee.kiwi.customization.placement.PlaceSlotProvider;
import snownee.kiwi.customization.placement.SlotLink;
import snownee.kiwi.customization.shape.ShapeStorage;
import snownee.kiwi.customization.shape.UnbakedShapeCodec;
import snownee.kiwi.util.resource.OneTimeLoader;

public record BlockFundamentals(
		Map<ResourceLocation, KMaterial> materials,
		Map<ResourceLocation, KBlockTemplate> templates,
		PlaceSlotProvider.Preparation slotProviders,
		SlotLink.Preparation slotLinks,
		PlaceChoices.Preparation placeChoices,
		ShapeStorage shapes,
		Map<ResourceLocation, KBlockDefinition> blocks,
		MapCodec<Optional<KMaterial>> materialCodec) {
	public static BlockFundamentals reload(ResourceManager resourceManager, boolean booting) {
		var materials = OneTimeLoader.load(resourceManager, "kiwi/material", KMaterial.DIRECT_CODEC);
		MapCodec<Optional<KMaterial>> materialCodec = CustomizationCodecs.strictOptionalField(CustomizationCodecs.simpleByNameCodec(
				materials), "material");
		var templates = OneTimeLoader.load(resourceManager, "kiwi/template/block", KBlockTemplate.codec(materialCodec));
		if (booting) {
			templates.forEach((key, value) -> value.resolve(key));
		}
		var slotProviders = PlaceSlotProvider.Preparation.of(() -> OneTimeLoader.load(
				resourceManager,
				"kiwi/placement/slot",
				PlaceSlotProvider.CODEC), templates);
		var slotLinks = SlotLink.Preparation.of(() -> OneTimeLoader.load(
				resourceManager,
				"kiwi/placement/link",
				SlotLink.CODEC), slotProviders);
		var placeChoices = PlaceChoices.Preparation.of(() -> OneTimeLoader.load(
				resourceManager,
				"kiwi/placement/choices",
				PlaceChoices.CODEC), templates);
		var shapes = ShapeStorage.reload(() -> OneTimeLoader.load(
				resourceManager,
				"kiwi/shape",
				new UnbakedShapeCodec()));
		var blocks = OneTimeLoader.load(
				resourceManager,
				"kiwi/block",
				KBlockDefinition.codec(templates, materialCodec));
		return new BlockFundamentals(materials, templates, slotProviders, slotLinks, placeChoices, shapes, blocks, materialCodec);
	}
}
