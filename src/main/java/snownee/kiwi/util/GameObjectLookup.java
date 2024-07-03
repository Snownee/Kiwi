package snownee.kiwi.util;

import java.util.Objects;
import java.util.stream.Stream;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import snownee.kiwi.KiwiGOHolder;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.KiwiModules;

public interface GameObjectLookup {

	static <T> Stream<T> all(ResourceKey<Registry<T>> registryKey, String modId) {
		return allHolders(registryKey, modId).map(Holder::value);
	}

	@SuppressWarnings("unchecked")
	static <T> Stream<Holder.Reference<T>> allHolders(ResourceKey<Registry<T>> registryKey, String modId) {
		Registry<T> registry = (Registry<T>) Objects.requireNonNull(BuiltInRegistries.REGISTRY.get(registryKey.location()));
		return registry.holders().filter($ -> $.key().location().getNamespace().equals(modId));
	}

	static <T> Stream<OptionalEntry<T>> fromModules(ResourceKey<Registry<T>> registryKey, String... ids) {
		/* off */
		return Stream.of(ids)
				.map(ResourceLocation::parse)
				.map(KiwiModules::get)
				.mapMulti(($, consumer) -> {
					boolean optional = $.module.getClass().getDeclaredAnnotation(KiwiModule.Optional.class) != null;
					$.getRegistryEntries(registryKey)
							.map($$ -> new OptionalEntry<>($$, optional))
							.forEach(consumer);
				});
	}

	record OptionalEntry<T>(KiwiGOHolder<T> holder, boolean optional) {
	}

}
