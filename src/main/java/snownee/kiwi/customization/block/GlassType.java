package snownee.kiwi.customization.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import snownee.kiwi.RenderLayerEnum;
import snownee.kiwi.util.codec.CustomizationCodecs;

public record GlassType(boolean skipRendering, float shadeBrightness, RenderLayerEnum renderType) {
	public static final Codec<GlassType> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("skip_rendering", true).forGetter(GlassType::skipRendering),
			Codec.floatRange(0, 1).optionalFieldOf("shade_brightness", 1F).forGetter(GlassType::shadeBrightness),
			CustomizationCodecs.RENDER_TYPE.fieldOf("render_type").forGetter(GlassType::renderType)
	).apply(instance, GlassType::new));
}
