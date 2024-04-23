package snownee.kiwi.customization.block.loader;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public record KCreativeTab(int order, ResourceKey<Item> icon, List<ResourceKey<Item>> contents) {
	public static final Codec<KCreativeTab> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf("order", 0).forGetter(KCreativeTab::order),
			ResourceKey.codec(Registries.ITEM)
					.optionalFieldOf("icon", ResourceKey.create(Registries.ITEM, new ResourceLocation("barrier")))
					.forGetter(KCreativeTab::icon),
			Codec.list(ResourceKey.codec(Registries.ITEM)).optionalFieldOf("contents", List.of()).forGetter(KCreativeTab::contents)
	).apply(instance, KCreativeTab::new));
}
