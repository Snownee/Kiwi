package snownee.kiwi.util;

import java.util.stream.Stream;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import snownee.kiwi.KiwiGO;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.KiwiModules;

public interface GameObjectLookup {

	static <T> Stream<T> all(HolderLookup.RegistryLookup<T> registry, String modId) {
		return allHolders(registry, modId).map(Holder::value);
	}

	static <T> Stream<T> all(HolderLookup.Provider registries, ResourceKey<Registry<T>> registryKey, String modId) {
		return allHolders(registries, registryKey, modId).map(Holder::value);
	}

	static <T> Stream<Holder.Reference<T>> allHolders(HolderLookup.RegistryLookup<T> registry, String modId) {
		return registry.listElements().filter($ -> $.key().identifier().getNamespace().equals(modId));
	}

	static <T> Stream<Holder.Reference<T>> allHolders(
			HolderLookup.Provider registries,
			ResourceKey<Registry<T>> registryKey,
			String modId) {
		return allHolders(registries.lookupOrThrow(registryKey), modId);
	}

	static <T> Stream<OptionalEntry<T>> fromModules(ResourceKey<Registry<T>> registryKey, String... ids) {
		/* off */
		return Stream.of(ids)
				.map(Identifier::parse)
				.map(KiwiModules::get)
				.mapMulti(($, consumer) -> {
					boolean optional = $.module.getClass().getDeclaredAnnotation(KiwiModule.Optional.class) != null;
					$.getRegistryEntries(registryKey)
							.map($$ -> new OptionalEntry<>($$, optional))
							.forEach(consumer);
				});
	}

	record OptionalEntry<T>(KiwiGO<T> holder, boolean optional) {
	}

}
