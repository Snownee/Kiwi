package snownee.kiwi.mixin.customization.fabric;

import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.Sets;

import net.fabricmc.fabric.impl.resource.loader.ModResourcePackCreator;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import snownee.kiwi.customization.CustomizationServiceFinder;
import snownee.kiwi.util.resource.RequiredFolderRepositorySource;

@Mixin(value = PackRepository.class, priority = 2000)
public class PackRepositoryMixin {
	@Mutable
	@Shadow
	@Final
	private Set<RepositorySource> sources;

	@SuppressWarnings("UnstableApiUsage")
	@Inject(method = "<init>", at = @At("RETURN"))
	private void kiwi$init(RepositorySource[] repositorySources, CallbackInfo ci) {
		PackType packType = null;
		boolean hasModSource = false;
		for (RepositorySource source : repositorySources) {
			if (packType == null) {
				if (source instanceof FolderRepositorySource) {
					PackType t = ((FolderRepositorySource) source).packType;
					if (t == PackType.SERVER_DATA || t == PackType.CLIENT_RESOURCES) {
						packType = t;
					}
				} else if (source instanceof ServerPacksSource) {
					packType = PackType.SERVER_DATA;
				}
			}
			if (source instanceof ModResourcePackCreator) {
				hasModSource = true;
			}
		}
		if (packType != null) {
			if (sources instanceof ImmutableCollection) {
				sources = Sets.newLinkedHashSet(sources);
			}
			if (!hasModSource) {
				if (packType == PackType.CLIENT_RESOURCES) {
					sources.add(ModResourcePackCreator.CLIENT_RESOURCE_PACK_PROVIDER);
				} else {
					sources.add(new ModResourcePackCreator(PackType.SERVER_DATA));
				}
			}
			sources.add(new RequiredFolderRepositorySource(
					CustomizationServiceFinder.PACK_DIRECTORY,
					packType,
					PackSource.BUILT_IN));
		}
	}
}
