package snownee.kiwi.customization.block.loader;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import snownee.kiwi.util.codec.CustomizationCodecs;

public record KMaterial(
		float destroyTime,
		float explosionResistance,
		SoundType soundType,
		MapColor defaultMapColor,
		NoteBlockInstrument instrument,
		boolean requiresCorrectToolForDrops,
		boolean ignitedByLava,
		int igniteOdds,
		int burnOdds) {
	public static final Codec<KMaterial> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.optionalFieldOf("destroy_time", 1.5f).forGetter(KMaterial::destroyTime),
			Codec.FLOAT.optionalFieldOf("explosion_resistance", 6f).forGetter(KMaterial::explosionResistance),
			CustomizationCodecs.SOUND_TYPE_CODEC.optionalFieldOf("sound_type", SoundType.STONE).forGetter(KMaterial::soundType),
			CustomizationCodecs.MAP_COLOR_CODEC.optionalFieldOf("map_color", MapColor.STONE).forGetter(KMaterial::defaultMapColor),
			CustomizationCodecs.INSTRUMENT_CODEC.optionalFieldOf("instrument", NoteBlockInstrument.HARP).forGetter(KMaterial::instrument),
			Codec.BOOL.optionalFieldOf("requires_correct_tool", false).forGetter(KMaterial::requiresCorrectToolForDrops),
			Codec.BOOL.optionalFieldOf("ignited_by_lava", false).forGetter(KMaterial::ignitedByLava),
			ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("ignite_odds", 0).forGetter(KMaterial::igniteOdds),
			ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("burn_odds", 0).forGetter(KMaterial::burnOdds)
	).apply(instance, KMaterial::new));
}
