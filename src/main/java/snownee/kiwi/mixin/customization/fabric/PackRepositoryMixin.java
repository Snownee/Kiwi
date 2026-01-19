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

import net.fabricmc.fabric.impl.resource.pack.ModResourcePackCreator;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.world.level.validation.DirectoryValidator;
import snownee.kiwi.customization.CustomizationServiceFinder;
import snownee.kiwi.util.resource.RequiredFolderRepositorySource;

@Mixin(value = PackRepository.class, priority = 2000)
public class PackRepositoryMixin {
	@Mutable
	@Shadow
	@Final
	private Set<RepositorySource> sources;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void kiwi$init(RepositorySource[] sources, CallbackInfo ci) {
		PackType packType = null;
		boolean hasModSource = false;
		for (RepositorySource source : sources) {
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
			if (this.sources instanceof ImmutableCollection) {
				this.sources = Sets.newLinkedHashSet(this.sources);
			}
			if (!hasModSource) {
				if (packType == PackType.CLIENT_RESOURCES) {
					this.sources.add(ModResourcePackCreator.CLIENT_RESOURCE_PACK_PROVIDER);
				} else {
					this.sources.add(new ModResourcePackCreator(PackType.SERVER_DATA));
				}
			}
			this.sources.add(new RequiredFolderRepositorySource(
					CustomizationServiceFinder.PACK_DIRECTORY,
					packType,
					PackSource.BUILT_IN,
					new DirectoryValidator(_ -> true)));
		}
	}
}
