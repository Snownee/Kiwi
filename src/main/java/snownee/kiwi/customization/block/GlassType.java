package snownee.kiwi.customization.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record GlassType(boolean skipRendering, float shadeBrightness) {
	public static final Codec<GlassType> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("skip_rendering", true).forGetter(GlassType::skipRendering),
			Codec.floatRange(0, 1).optionalFieldOf("shade_brightness", 1F).forGetter(GlassType::shadeBrightness)
	).apply(instance, GlassType::new));
}
