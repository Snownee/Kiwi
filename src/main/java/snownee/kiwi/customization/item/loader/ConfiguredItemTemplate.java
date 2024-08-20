package snownee.kiwi.customization.item.loader;

import java.util.Map;
import java.util.function.Function;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.codecs.KeyDispatchCodec;

import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import snownee.kiwi.customization.block.loader.ConfiguredBlockTemplate;
import snownee.kiwi.util.codec.CustomizationCodecs;

public record ConfiguredItemTemplate(KItemTemplate template, JsonObject json) {

	public static final JsonObject DEFAULT_JSON = new JsonObject();

	static {
		DEFAULT_JSON.add(ItemCodecs.ITEM_PROPERTIES_KEY, new JsonObject());
	}

	public ConfiguredItemTemplate(KItemTemplate template) {
		this(template, DEFAULT_JSON);
	}

	public static Codec<ConfiguredItemTemplate> codec(Map<ResourceLocation, KItemTemplate> templates) {
		Function<ConfiguredItemTemplate, DataResult<KItemTemplate>> type = $ -> DataResult.success($.template());
		Function<KItemTemplate, DataResult<MapCodec<ConfiguredItemTemplate>>> codec = $ -> DataResult.success(MapCodec.assumeMapUnsafe(ExtraCodecs.JSON).flatXmap(
				json -> DataResult.success(new ConfiguredItemTemplate($, json.getAsJsonObject())),
				template -> DataResult.error(() -> "Unsupported operation", template.json)
		));

		Codec<ConfiguredItemTemplate> codec1 = new KeyDispatchCodec<>(
				"kiwi:type",
				CustomizationCodecs.simpleByNameCodec(templates),
				type,
				codec
		).codec();
		Codec<ConfiguredItemTemplate> codec2 = ResourceLocation.CODEC.flatXmap(
				id -> {
					KItemTemplate template = templates.get(id);
					if (template == null) {
						return DataResult.error(() -> "Unknown template: " + id);
					}
					return DataResult.success(new ConfiguredItemTemplate(template));
				},
				template -> DataResult.error(() -> "Unsupported operation"));
		return CustomizationCodecs.withAlternative(codec1, codec2);
	}

}
