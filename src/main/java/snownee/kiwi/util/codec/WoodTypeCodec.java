package snownee.kiwi.util.codec;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;

import net.minecraft.world.level.block.state.properties.WoodType;

public class WoodTypeCodec {
	public static final BiMap<String, WoodType> BY_NAME = HashBiMap.create();

	public static final Codec<WoodType> CODEC = CustomizationCodecs.simpleByNameCodec(BY_NAME);
}
