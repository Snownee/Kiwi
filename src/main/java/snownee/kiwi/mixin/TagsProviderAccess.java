package snownee.kiwi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.core.Registry;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;

@Mixin(TagsProvider.class)
public interface TagsProviderAccess<T> {

	@Accessor
	ResourceKey<? extends Registry<T>> getRegistryKey();

	@Accessor(remap = false)
	String getModId();

	@Invoker
	TagBuilder callGetOrCreateRawBuilder(TagKey<T> tag);

}
