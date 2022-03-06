package snownee.kiwi.datagen.provider;

import java.util.function.Supplier;
import java.util.stream.Stream;

import net.minecraft.core.Registry;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.Tag;
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

	public void optional(TagKey<T> tag, Supplier<T>... blocks) {
		Tag.Builder builder = tagsProvider.callGetOrCreateRawBuilder(tag);
		for (Supplier<T> block : blocks) {
			builder.addElement(registry.getKey(block.get()), modId);
		}
	}

	public void add(TagKey<T> tag, Supplier<T>... blocks) {
		Tag.Builder builder = tagsProvider.callGetOrCreateRawBuilder(tag);
		for (Supplier<T> block : blocks) {
			builder.addOptionalElement(registry.getKey(block.get()), modId);
		}
	}

	public Stream<T> getModEntries() {
		return registry.stream().filter($ -> registry.getKey($).getNamespace().equals(modId));
	}

}
