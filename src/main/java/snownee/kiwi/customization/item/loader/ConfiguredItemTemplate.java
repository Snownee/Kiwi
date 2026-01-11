package snownee.kiwi.customization.item.loader;

import java.util.Map;
import java.util.function.Function;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.codecs.KeyDispatchCodec;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
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
		Function<KItemTemplate, DataResult<Codec<ConfiguredItemTemplate>>> codec = $ -> DataResult.success(ExtraCodecs.JSON.flatXmap(json -> {
			return DataResult.success(new ConfiguredItemTemplate($, json.getAsJsonObject()));
		}, template -> {
			return DataResult.error(() -> "Unsupported operation");
		}));
		return CustomizationCodecs.withAlternative(
				KeyDispatchCodec.unsafe(
						"kiwi:type",
						CustomizationCodecs.simpleByNameCodec(templates),
						type,
						codec,
						v -> getCodec(type, codec, v)
				).codec(),
				ResourceLocation.CODEC.flatXmap(
						id -> {
							KItemTemplate template = templates.get(id);
							if (template == null) {
								return DataResult.error(() -> "Unknown template: " + id);
							}
							return DataResult.success(new ConfiguredItemTemplate(template));
						},
						template -> DataResult.error(() -> "Unsupported operation"))
		);
	}

	@SuppressWarnings("unchecked")
	private static <K, V> DataResult<? extends Encoder<V>> getCodec(
			final Function<? super V, ? extends DataResult<? extends K>> type,
			final Function<? super K, ? extends DataResult<? extends Encoder<? extends V>>> encoder,
			final V input) {
		return type.apply(input).<Encoder<? extends V>>flatMap(k -> encoder.apply(k).map(Function.identity())).map(c -> ((Encoder<V>) c));
	}
}
