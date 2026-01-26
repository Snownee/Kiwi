package snownee.kiwi.util.resource;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.base.Preconditions;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public class AlternativesFileToIdConverter extends FileToIdConverter {
	private final String prefix;
	private final List<String> extensions;
	private final int sameExtensionLength;
	private Predicate<Identifier> listFilter;

	public AlternativesFileToIdConverter(String pPrefix, List<String> pExtensions) {
		this(pPrefix, pExtensions, _ -> true);
	}

	public AlternativesFileToIdConverter(String pPrefix, List<String> pExtensions, Predicate<Identifier> listFilter) {
		super(pPrefix, pExtensions.getFirst());
		this.prefix = pPrefix;
		this.extensions = pExtensions;
		sameExtensionLength = pExtensions.stream().mapToInt(String::length).distinct().reduce((a, b) -> -1).orElseThrow();
		Preconditions.checkArgument(!extensions.isEmpty(), "Extensions cannot be empty");
		this.listFilter = listFilter;
	}

	public AlternativesFileToIdConverter setListFilter(Predicate<Identifier> listFilter) {
		this.listFilter = listFilter;
		return this;
	}

	public static AlternativesFileToIdConverter yamlOrJson(String pName) {
		return new AlternativesFileToIdConverter(pName, List.of(".yaml", ".json"));
	}

	@Override
	public Identifier idToFile(Identifier pId) {
		return pId.withPath(prefix + "/" + pId.getPath() + extensions.getFirst());
	}

	public Stream<Identifier> idToAllPossibleFiles(Identifier pId) {
		return extensions.stream().map((ext) -> pId.withPath(prefix + "/" + pId.getPath() + ext));
	}

	@Override
	public Identifier fileToId(Identifier pFile) {
		if (sameExtensionLength >= 0) {
			String s = pFile.getPath();
			return pFile.withPath(s.substring(prefix.length() + 1, s.length() - sameExtensionLength));
		} else {
			for (String ext : extensions) {
				if (pFile.getPath().endsWith(ext)) {
					String s = pFile.getPath();
					return pFile.withPath(s.substring(prefix.length() + 1, s.length() - ext.length()));
				}
			}
			throw new IllegalArgumentException("Unknown extension for " + pFile);
		}
	}

	@Override
	public Map<Identifier, Resource> listMatchingResources(ResourceManager pResourceManager) {
		return pResourceManager.listResources(
				prefix,
				location -> extensions.stream().anyMatch(location.getPath()::endsWith) && listFilter.test(location));
	}

	@Override
	public Map<Identifier, List<Resource>> listMatchingResourceStacks(ResourceManager pResourceManager) {
		return pResourceManager.listResourceStacks(
				prefix,
				location -> extensions.stream().anyMatch(location.getPath()::endsWith) && listFilter.test(location));
	}
}