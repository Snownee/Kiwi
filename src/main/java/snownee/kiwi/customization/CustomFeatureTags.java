package snownee.kiwi.customization;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.Kiwi;

public class CustomFeatureTags {
	public static final TagKey<Block> SUSTAIN_PLANT = AbstractModule.blockTag(Kiwi.ID, "sustain_plant");
	public static final TagKey<Block> SITTABLE = AbstractModule.blockTag(Kiwi.ID, "sittable");
}
