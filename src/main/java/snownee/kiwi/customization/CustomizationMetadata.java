package snownee.kiwi.customization;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import snownee.kiwi.Kiwi;
import snownee.kiwi.util.KUtil;
import snownee.kiwi.util.resource.AlternativesFileToIdConverter;
import snownee.kiwi.util.resource.OneTimeLoader;

public record CustomizationMetadata(ImmutableListMultimap<String, String> registryOrder, List<String> lenientBETypeNamespaces) {
	public static final Codec<CustomizationMetadata> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
			Codec.unboundedMap(Codec.STRING, Codec.STRING.listOf())
					.fieldOf("registry_order")
					.forGetter($ -> {
						Map<String, List<String>> map = Maps.newHashMap();
						for (Map.Entry<String, Collection<String>> entry : $.registryOrder().asMap().entrySet()) {
							map.put(entry.getKey(), List.copyOf(entry.getValue()));
						}
						return map;
					}),
			Codec.STRING.listOf()
					.optionalFieldOf("lenient_be_type_namespaces", List.of())
					.forGetter(CustomizationMetadata::lenientBETypeNamespaces)
	).apply(instance, CustomizationMetadata::create));

	public static CustomizationMetadata create(Map<String, List<String>> map, List<String> lenientBETypeNamespaces) {
		ImmutableListMultimap.Builder<String, String> builder = ImmutableListMultimap.builder();
		map.forEach(builder::putAll);
		return new CustomizationMetadata(builder.build(), lenientBETypeNamespaces);
	}

	public static Map<String, CustomizationMetadata> loadMap(ResourceManager resourceManager, OneTimeLoader.Context context) {
		CustomizationMetadata emptyMetadata = new CustomizationMetadata(ImmutableListMultimap.of(), List.of());
		var fileToIdConverter = AlternativesFileToIdConverter.yamlOrJson(Kiwi.ID);
		Map<String, CustomizationMetadata> metadataMap = Maps.newHashMap();
		for (String namespace : resourceManager.getNamespaces()) {
			Identifier file = fileToIdConverter.idToFile(Identifier.fromNamespaceAndPath(namespace, "metadata"));
			Optional<Resource> resource = resourceManager.getResource(file);
			if (resource.isEmpty()) {
				metadataMap.put(namespace, emptyMetadata);
				continue;
			}
			DataResult<CustomizationMetadata> result = OneTimeLoader.parseFile(
					file,
					resource.get(),
					CustomizationMetadata.CODEC,
					context);
			if (result == null) { // condition not met
				context.addDisabledNamespace(namespace);
				continue;
			}
			result.result().ifPresentOrElse(
					customizationMetadata -> metadataMap.put(namespace, customizationMetadata),
					() -> context.addDisabledNamespace(namespace));
		}
		return metadataMap;
	}

	public static <T> void sortedForEach(
			Map<String, CustomizationMetadata> metadataMap,
			String key,
			Map<Identifier, T> values,
			BiConsumer<Identifier, T> action) {
		sortedForEach(metadataMap, List.of(key), values, action);
	}

	public static <T> void sortedForEach(
			Map<String, CustomizationMetadata> metadataMap,
			List<String> keys,
			Map<Identifier, T> values,
			BiConsumer<Identifier, T> action) {
		Set<Identifier> order = Sets.newLinkedHashSet();
		for (String key : keys) {
			metadataMap.forEach((namespace, metadata) -> {
				for (String s : metadata.registryOrder().get(key)) {
					order.add(KUtil.RL(s, namespace));
				}
			});
		}
		for (Identifier id : order) {
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