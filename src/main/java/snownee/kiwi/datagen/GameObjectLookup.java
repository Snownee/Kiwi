package snownee.kiwi.datagen;

import java.util.Objects;
import java.util.stream.Stream;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.KiwiModules;

public interface GameObjectLookup {

	@SuppressWarnings("unchecked")
	static <T> Stream<T> all(ResourceKey<Registry<T>> registryKey, String modId) {
		Registry<T> registry = (Registry<T>) Objects.requireNonNull(BuiltInRegistries.REGISTRY.get(registryKey.location()));
		return registry.holders().filter($ -> $.key().location().getNamespace().equals(modId)).map(Holder::value);
	}

	static <T> Stream<OptionalEntry<T>> fromModules(ResourceKey<Registry<T>> registryKey, String... ids) {
		/* off */
		return Stream.of(ids)
				.map(ResourceLocation::new)
				.map(KiwiModules::get)
				.mapMulti(($, consumer) -> {
					boolean optional = $.module.getClass().getDeclaredAnnotation(KiwiModule.Optional.class) != null;
					$.getRegistries(registryKey).stream()
							.map($$ -> new OptionalEntry<>($$, optional))
							.forEach(consumer);
				});
	}

	record OptionalEntry<T>(T object, boolean optional) {
	}

}
