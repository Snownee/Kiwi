package snownee.kiwi.customization.item.loader;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public record KCreativeTab(
		int order,
		ResourceKey<Item> icon,
		Optional<ResourceKey<CreativeModeTab>> insert,
		List<ResourceKey<Item>> contents) {
	public static final Codec<KCreativeTab> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf("order", 0).forGetter(KCreativeTab::order),
			ResourceKey.codec(Registries.ITEM)
					.optionalFieldOf("icon")
					.forGetter($ -> Optional.ofNullable($.icon())),
			ResourceKey.codec(Registries.CREATIVE_MODE_TAB)
					.optionalFieldOf("insert")
					.forGetter(KCreativeTab::insert),
			ExtraCodecs.nonEmptyList(Codec.list(ResourceKey.codec(Registries.ITEM)))
					.optionalFieldOf("contents", List.of())
					.forGetter(KCreativeTab::contents)
	).apply(instance, KCreativeTab::create));

	public static KCreativeTab create(
			int order,
			Optional<ResourceKey<Item>> icon,
			Optional<ResourceKey<CreativeModeTab>> insert,
			List<ResourceKey<Item>> contents) {
		if (icon.isPresent() && insert.isPresent()) {
			throw new IllegalArgumentException("Both icon and insert are present");
		}
		return new KCreativeTab(order, icon.orElse(contents.get(0)), insert, contents);
	}
}
