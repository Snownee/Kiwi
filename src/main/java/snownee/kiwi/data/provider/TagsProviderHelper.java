package snownee.kiwi.data.provider;

import java.util.stream.Stream;

import net.minecraft.core.Registry;
import net.minecraft.data.tags.TagsProvider.TagAppender;
import net.minecraftforge.registries.IForgeRegistryEntry;

public final class TagsProviderHelper {

	public static <T extends IForgeRegistryEntry<T>> TagAppender<T> addOptional(TagAppender<T> builder, T... blocks) {
		for (T block : blocks) {
			builder.addOptional(block.getRegistryName());
		}
		return builder;
	}

	public static <T extends IForgeRegistryEntry<T>> Stream<T> getModEntries(String modId, Registry<T> registry) {
		return registry.stream().filter($ -> $.getRegistryName().getNamespace().equals(modId));
	}

}
