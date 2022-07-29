package snownee.kiwi.datagen.provider;

import java.util.function.Supplier;
import java.util.stream.Stream;

import net.minecraft.core.Registry;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import snownee.kiwi.mixin.TagsProviderAccess;

public final class TagsProviderHelper<T> {

	private final TagsProviderAccess<T> tagsProvider;
	private final String modId;
	private final Registry<T> registry;

	public TagsProviderHelper(TagsProvider<T> tagsProvider) {
		this.tagsProvider = (TagsProviderAccess<T>) tagsProvider;
		modId = this.tagsProvider.getModId();
		registry = this.tagsProvider.getRegistry();
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

	public Stream<T> getModEntries() {
		return registry.stream().filter($ -> registry.getKey($).getNamespace().equals(modId));
	}

}
