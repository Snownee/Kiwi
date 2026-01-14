package snownee.kiwi.customization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceFilterSection;
import net.minecraft.server.packs.resources.ResourceManager;
import snownee.kiwi.Kiwi;

public class KiwiPackResourceManager implements CloseableResourceManager {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Map<String, FallbackResourceManager> namespacedManagers;
	private final List<PackResources> packs;

	public KiwiPackResourceManager(List<PackResources> packs) {
		this.packs = packs.stream().filter(pack -> {
			//noinspection ConstantValue
			if (pack.getNamespaces(PackType.CLIENT_RESOURCES) == null) {
				Kiwi.LOGGER.error("Pack {}({}) has null namespaces", pack.packId(), pack.getClass());
				return false;
			}
			return true;
		}).toList();
		packs = this.packs;

		Map<String, FallbackResourceManager> namespacedManagers = new HashMap<>();
		List<String> namespaces = packs.stream().flatMap(p -> p.getNamespaces(PackType.CLIENT_RESOURCES).stream()).distinct().toList();

		for (PackResources pack : packs) {
			ResourceFilterSection filterSection = this.getPackFilterSection(pack);
			Set<String> providedNamespaces = pack.getNamespaces(PackType.CLIENT_RESOURCES);
			Predicate<Identifier> pathFilter = filterSection != null ? location -> filterSection.isPathFiltered(location.getPath()) : null;

			for (String namespace : namespaces) {
				boolean packContainsNamespace = providedNamespaces.contains(namespace);
				boolean filterMatchesNamespace = filterSection != null && filterSection.isNamespaceFiltered(namespace);
				if (packContainsNamespace || filterMatchesNamespace) {
					FallbackResourceManager fallbackResourceManager = namespacedManagers.computeIfAbsent(
							namespace,
							n -> new FallbackResourceManager(PackType.CLIENT_RESOURCES, n));

					if (packContainsNamespace && filterMatchesNamespace) {
						fallbackResourceManager.push(pack, pathFilter);
					} else if (packContainsNamespace) {
						fallbackResourceManager.push(pack);
					} else {
						fallbackResourceManager.pushFilterOnly(pack.packId(), pathFilter);
					}
				}
			}
		}

		this.namespacedManagers = namespacedManagers;
	}

	@Nullable
	private ResourceFilterSection getPackFilterSection(final PackResources pack) {
		try {
			return pack.getMetadataSection(ResourceFilterSection.TYPE);
		} catch (Exception var3) {
			LOGGER.error("Failed to get filter section from pack {}", pack.packId());
			return null;
		}
	}

	@Override
	public Set<String> getNamespaces() {
		return this.namespacedManagers.keySet();
	}

	@Override
	public Optional<Resource> getResource(final Identifier location) {
		ResourceManager pack = this.namespacedManagers.get(location.getNamespace());
		return pack != null ? pack.getResource(location) : Optional.empty();
	}

	@Override
	public List<Resource> getResourceStack(final Identifier location) {
		ResourceManager pack = this.namespacedManagers.get(location.getNamespace());
		return pack != null ? pack.getResourceStack(location) : List.of();
	}

	@Override
	public Map<Identifier, Resource> listResources(final String directory, final Predicate<Identifier> filter) {
		checkTrailingDirectoryPath(directory);
		Map<Identifier, Resource> result = new TreeMap<>();

		for (FallbackResourceManager manager : this.namespacedManagers.values()) {
			result.putAll(manager.listResources(directory, filter));
		}

		return result;
	}

	@Override
	public Map<Identifier, List<Resource>> listResourceStacks(final String directory, final Predicate<Identifier> filter) {
		checkTrailingDirectoryPath(directory);
		Map<Identifier, List<Resource>> result = new TreeMap<>();

		for (FallbackResourceManager manager : this.namespacedManagers.values()) {
			result.putAll(manager.listResourceStacks(directory, filter));
		}

		return result;
	}

	private static void checkTrailingDirectoryPath(final String directory) {
		if (directory.endsWith("/")) {
			throw new IllegalArgumentException("Trailing slash in path " + directory);
		}
	}

	@Override
	public Stream<PackResources> listPacks() {
		return this.packs.stream();
	}

	@Override
	public void close() {
		this.packs.forEach(PackResources::close);
	}
}