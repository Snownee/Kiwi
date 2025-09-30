package snownee.kiwi.customization.block;

import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.MapCodec;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import snownee.kiwi.customization.block.loader.KBlockDefinition;
import snownee.kiwi.customization.block.loader.KBlockTemplate;
import snownee.kiwi.customization.block.loader.KMaterial;
import snownee.kiwi.customization.placement.PlaceChoices;
import snownee.kiwi.customization.placement.PlaceSlotProvider;
import snownee.kiwi.customization.placement.SlotLink;
import snownee.kiwi.customization.shape.ShapeStorage;
import snownee.kiwi.customization.shape.UnbakedShapeCodec;
import snownee.kiwi.util.codec.CustomizationCodecs;
import snownee.kiwi.util.resource.OneTimeLoader;

public record BlockFundamentals(
		Map<ResourceLocation, KMaterial> materials,
		Map<ResourceLocation, GlassType> glassTypes,
		Map<ResourceLocation, KBlockTemplate> templates,
		PlaceSlotProvider.Preparation slotProviders,
		SlotLink.Preparation slotLinks,
		PlaceChoices.Preparation placeChoices,
		ShapeStorage shapes,
		Map<ResourceLocation, KBlockDefinition> blocks) {
	public static BlockFundamentals reload(ResourceManager resourceManager, OneTimeLoader.Context context, boolean booting) {
		var materials = OneTimeLoader.load(resourceManager, "kiwi/material", KMaterial.DIRECT_CODEC, context);
		MapCodec<Optional<KMaterial>> materialCodec = CustomizationCodecs.simpleByNameCodec(materials).optionalFieldOf("material");
		var glassTypes = OneTimeLoader.load(resourceManager, "kiwi/glass_type", GlassType.DIRECT_CODEC, context);
		MapCodec<Optional<GlassType>> glassTypeCodec = CustomizationCodecs.simpleByNameCodec(glassTypes).optionalFieldOf("glass_type");
		CodecCreationContext creationContext = new CodecCreationContext(materialCodec, glassTypeCodec);
		var templates = OneTimeLoader.load(resourceManager, "kiwi/template/block", KBlockTemplate.codec(creationContext), context);
		if (booting) {
			templates.forEach((key, value) -> value.resolve(key, context));
		}
		var slotProviders = PlaceSlotProvider.Preparation.of(() -> OneTimeLoader.load(
				resourceManager,
				"kiwi/placement/slot",
				PlaceSlotProvider.CODEC,
				context), templates);
		var slotLinks = SlotLink.Preparation.of(() -> OneTimeLoader.load(
				resourceManager,
				"kiwi/placement/link",
				SlotLink.CODEC,
				context), slotProviders);
		var placeChoices = PlaceChoices.Preparation.of(() -> OneTimeLoader.load(
				resourceManager,
				"kiwi/placement/choices",
				PlaceChoices.CODEC,
				context), templates);
		var shapes = ShapeStorage.reload(() -> OneTimeLoader.load(
				resourceManager,
				"kiwi/shape",
				new UnbakedShapeCodec(),
				context));
		var blocks = OneTimeLoader.load(
				resourceManager,
				"kiwi/block",
				KBlockDefinition.codec(templates, creationContext),
				context);
		return new BlockFundamentals(
				materials,
				glassTypes,
				templates,
				slotProviders,
				slotLinks,
				placeChoices,
				shapes,
				blocks);
	}

	public record CodecCreationContext(MapCodec<Optional<KMaterial>> materialCodec, MapCodec<Optional<GlassType>> glassTypeCodec) {}
}
