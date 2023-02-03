package snownee.kiwi.datagen.provider;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.RegistryManager;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.KiwiModules;
import snownee.kiwi.mixin.TagsProviderAccess;

public final class TagsProviderHelper<T> {

	private final TagsProviderAccess<T> tagsProvider;
	private final String modId;
	private final Registry<T> registry;

	public TagsProviderHelper(TagsProvider<T> tagsProvider) {
		this.tagsProvider = (TagsProviderAccess<T>) tagsProvider;
		modId = this.tagsProvider.getModId();
		registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(this.tagsProvider.getRegistryKey().location());
	}

	public void optional(TagKey<T> tag, Supplier<? extends T>... blocks) {
		TagBuilder builder = tagsProvider.callGetOrCreateRawBuilder(tag);
		for (Supplier<? extends T> block : blocks) {
			builder.addOptionalElement(registry.getKey(block.get()));
		}
	}

	public void add(TagKey<T> tag, Supplier<? extends T>... blocks) {
		TagBuilder builder = tagsProvider.callGetOrCreateRawBuilder(tag);
		for (Supplier<? extends T> block : blocks) {
			builder.addElement(registry.getKey(block.get()));
		}
	}

	public void add(TagKey<T> tag, OptionalEntry<T> entry) {
		TagBuilder builder = tagsProvider.callGetOrCreateRawBuilder(tag);
		if (entry.optional) {
			builder.addOptionalElement(registry.getKey(entry.object));
		} else {
			builder.addElement(registry.getKey(entry.object));
		}
	}

	public Stream<T> getAllEntries() {
		return registry.stream().filter($ -> registry.getKey($).getNamespace().equals(modId));
	}

	public Stream<OptionalEntry<T>> getEntriesByModule(String... ids) {
		Object key = Optional.<Object>ofNullable(RegistryManager.ACTIVE.getRegistry(registry.key())).orElse(registry);
		/* off */
		return Stream.of(ids)
				.map(ResourceLocation::new)
				.map(KiwiModules::get)
				.mapMulti(($, consumer) -> {
					boolean optional = $.module.getClass().getDeclaredAnnotation(KiwiModule.Optional.class) != null;
					$.getRegistries(key).stream()
							.map($$ -> (T) $$)
							.map($$ -> new OptionalEntry<>($$, optional));
				});
	}

	public record OptionalEntry<T> (T object, boolean optional) {
	}

}
