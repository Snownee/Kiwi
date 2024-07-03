package snownee.kiwi.util.resource;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public class AlternativesFileToIdConverter {
	private final String prefix;
	private final List<String> extensions;
	private final int sameExtensionLength;
	private Predicate<ResourceLocation> listFilter;

	public AlternativesFileToIdConverter(String pPrefix, List<String> pExtensions) {
		this(pPrefix, pExtensions, $ -> true);
	}

	public AlternativesFileToIdConverter(String pPrefix, List<String> pExtensions, Predicate<ResourceLocation> listFilter) {
		this.prefix = pPrefix;
		this.extensions = pExtensions;
		sameExtensionLength = pExtensions.stream().mapToInt(String::length).distinct().reduce((a, b) -> -1).orElseThrow();
		Preconditions.checkArgument(!extensions.isEmpty(), "Extensions cannot be empty");
		this.listFilter = listFilter;
	}

	public AlternativesFileToIdConverter setListFilter(Predicate<ResourceLocation> listFilter) {
		this.listFilter = listFilter;
		return this;
	}

	public static AlternativesFileToIdConverter yamlOrJson(String pName) {
		return new AlternativesFileToIdConverter(pName, List.of(".yaml", ".json"));
	}

	public ResourceLocation idToFile(ResourceLocation pId) {
		return pId.withPath(this.prefix + "/" + pId.getPath() + extensions.get(0));
	}

	public Stream<ResourceLocation> idToAllPossibleFiles(ResourceLocation pId) {
		return extensions.stream().map((ext) -> pId.withPath(this.prefix + "/" + pId.getPath() + ext));
	}

	public ResourceLocation fileToId(ResourceLocation pFile) {
		if (sameExtensionLength >= 0) {
			String s = pFile.getPath();
			return pFile.withPath(s.substring(this.prefix.length() + 1, s.length() - sameExtensionLength));
		} else {
			for (String ext : extensions) {
				if (pFile.getPath().endsWith(ext)) {
					String s = pFile.getPath();
					return pFile.withPath(s.substring(this.prefix.length() + 1, s.length() - ext.length()));
				}
			}
			throw new IllegalArgumentException("Unknown extension for " + pFile);
		}
	}

	public Map<ResourceLocation, Resource> listMatchingResources(ResourceManager pResourceManager) {
		return pResourceManager.listResources(this.prefix, (location) -> {
			return this.extensions.stream().anyMatch(location.getPath()::endsWith) && listFilter.test(location);
		});
	}

	public Map<ResourceLocation, List<Resource>> listMatchingResourceStacks(ResourceManager pResourceManager) {
		return pResourceManager.listResourceStacks(this.prefix, (location) -> {
			return this.extensions.stream().anyMatch(location.getPath()::endsWith) && listFilter.test(location);
		});
	}
}