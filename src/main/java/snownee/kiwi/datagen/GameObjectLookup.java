package snownee.kiwi.datagen;

import java.util.stream.Stream;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.KiwiModules;

public interface GameObjectLookup {

	static <T> Stream<T> all(ResourceKey<Registry<T>> type, String modId) {
		Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(type.location());
		return registry.stream().filter($ -> registry.getKey($).getNamespace().equals(modId));
	}

	static <T> Stream<OptionalEntry<T>> fromModules(ResourceKey<Registry<T>> type, String... ids) {
		Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(type.location());
		/* off */
		return Stream.of(ids)
				.map(ResourceLocation::new)
				.map(KiwiModules::get)
				.mapMulti(($, consumer) -> {
					boolean optional = $.module.getClass().getDeclaredAnnotation(KiwiModule.Optional.class) != null;
					$.getRegistries(registry).stream()
							.map($$ -> (T) $$)
							.map($$ -> new OptionalEntry<>($$, optional))
							.forEach(consumer);
				});
	}

	record OptionalEntry<T>(T object, boolean optional) {
	}

}
