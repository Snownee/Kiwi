package snownee.kiwi.customization;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import snownee.kiwi.Kiwi;
import snownee.kiwi.util.Util;
import snownee.kiwi.util.resource.OneTimeLoader;

public record CustomizationMetadata(ImmutableListMultimap<String, String> registryOrder) {
	public static final Codec<CustomizationMetadata> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
			Codec.unboundedMap(Codec.STRING, Codec.STRING.listOf())
					.fieldOf("registry_order")
					.forGetter($ -> {
						Map<String, List<String>> map = Maps.newHashMap();
						for (Map.Entry<String, Collection<String>> entry : $.registryOrder().asMap().entrySet()) {
							map.put(entry.getKey(), List.copyOf(entry.getValue()));
						}
						return map;
					})
	).apply(instance, CustomizationMetadata::create));

	public static CustomizationMetadata create(Map<String, List<String>> map) {
		ImmutableListMultimap.Builder<String, String> builder = ImmutableListMultimap.builder();
		map.forEach(builder::putAll);
		return new CustomizationMetadata(builder.build());
	}

	public static Map<String, CustomizationMetadata> loadMap(ResourceManager resourceManager, Set<String> namespaces) {
		CustomizationMetadata emptyMetadata = new CustomizationMetadata(ImmutableListMultimap.of());
		return namespaces.stream().collect(Collectors.toUnmodifiableMap(
				Function.identity(),
				$ -> Optional.ofNullable(OneTimeLoader.loadFile(
						resourceManager,
						Kiwi.ID,
						new ResourceLocation($, "metadata"),
						CustomizationMetadata.CODEC)).orElse(emptyMetadata)
		));
	}

	public static <T> void sortedForEach(
			Map<String, CustomizationMetadata> metadataMap,
			String key,
			Map<ResourceLocation, T> values,
			BiConsumer<ResourceLocation, T> action) {
		Set<ResourceLocation> order = Sets.newLinkedHashSet();
		metadataMap.forEach((namespace, metadata) -> {
			for (String s : metadata.registryOrder().get(key)) {
				order.add(Util.RL(s, namespace));
			}
		});
		for (ResourceLocation id : order) {
			T value = values.get(id);
			if (value != null) {
				action.accept(id, value);
			}
		}
		values.forEach((id, value) -> {
			if (!order.contains(id)) {
				action.accept(id, value);
			}
		});
	}
}
